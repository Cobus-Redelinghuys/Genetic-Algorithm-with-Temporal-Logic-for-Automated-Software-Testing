import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ModulesConfig {
    public static ModuleConfig[] moduleConfigs;

    public static void set(String path) {
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try {
            obj = jsonParser.parse(new FileReader(path + "ModuleConfig.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray jsonArray = (JSONArray) jsonObject.get("modules");
        ArrayList<ModuleConfig> tempArr = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jObject = (JSONObject) jsonArray.get(i);
            tempArr.add(new ModuleConfig(jObject, path));
        }
        moduleConfigs = tempArr.toArray(new ModuleConfig[0]);
    }

    @SuppressWarnings("unchecked")
    public static JSONArray executeSystem(Input input, String path) {
        JSONArray result = new JSONArray();
        /*
         * for(int i=0; i < moduleConfigs.length; i++){
         * result.add(moduleConfigs[i].executeModule(input.moduleInputs.get(
         * moduleConfigs[i].moduleName)));
         * }
         */
        ModuleRunner[] moduleRunners = new ModuleRunner[moduleConfigs.length];
        HashMap<Integer, JSONObject> tempResults = new HashMap<>();
        for (int i = 0; i < moduleRunners.length; i++) {
            moduleRunners[i] = new ModuleRunner(moduleConfigs[i],
                    createFile(input.moduleInputs),
                    path);
            moduleRunners[i].run();
        }
        try {
            for (int i = 0; i < moduleRunners.length; i++) {
                if (moduleRunners[i].moduleConfig.enabled) {
                    // moduleRunners[i].join();
                    tempResults.put(i, moduleRunners[i].getResults());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < moduleRunners.length; i++) {
            if (tempResults.containsKey(i)) {
                result.add(tempResults.get(i));
            }
        }

        return result;
    }

    static String createFile(ArrayList<Long> input){
        String result = "";
        for (Long element : input) {
            result += element.toString() + "\n";
        }
        return result;
    }
}

class ModuleConfig {
    public final String moduleName;
    public final boolean enabled;
    public final String executablePath;
    public final String executionCommand;
    public final int numberOfCLArguments;
    public final String relativePath;

    public ModuleConfig(JSONObject object, String relativePath) {
        Object res = null;
        try {
            res = object.get("moduleName");
        } catch (Exception e) {
            e.printStackTrace();
            res = "module1";
        } finally {
            moduleName = (String) res;
        }

        try {
            res = object.get("enabled");
        } catch (Exception e) {
            e.printStackTrace();
            res = true;
        } finally {
            enabled = (Boolean) res;
        }

        try {
            res = object.get("relativePath");
        } catch (Exception e) {
            e.printStackTrace();
            res = true;
        } finally {
            executablePath = (String) res;
        }

        try {
            res = object.get("executionCommand");
        } catch (Exception e) {
            e.printStackTrace();
            res = true;
        } finally {
            executionCommand = (String) res;
        }

        try {
            res = object.get("numberOfCLArguments");
        } catch (Exception e) {
            e.printStackTrace();
            res = true;
        } finally {
            numberOfCLArguments = ((Long) res).intValue();
        }

        this.relativePath = relativePath;
    }

    @SuppressWarnings("unchecked")
    public JSONObject executeModule(String input, String path) {
        String[] result = new String[2];
        LocalTime start = null;
        LocalTime end;
        int exitValue = 0;
        try {
            FileWriter myWriter = new FileWriter(path + "/input.txt");
            myWriter.write(input);
            myWriter.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            start = LocalTime.now();
            Runtime runtime = Runtime.getRuntime();
            String command = "make all -C ./SUT/ -s";
            System.out.println(command);
            Process process = runtime
                    .exec(command);
            InputStream inputStream = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            InputStream errorStream = process.getErrorStream();
            InputStreamReader esr = new InputStreamReader(errorStream);
            result[0] = "";
            result[1] = "";
            int n1;
            ProcessMonitor processMonitor = new ProcessMonitor(process, start);
            processMonitor.start();
            while ((n1 = isr.read()) > 0) {
                result[0] += (char) n1;
            }
            while ((n1 = esr.read()) > 0) {
                result[1] += (char) n1;
            }
            while (process.isAlive()) {
            }
            processMonitor.join();
            if (processMonitor.infiniteLoop)
                result[1] += "Infinite loop";
            if (process.exitValue() == 139)
                result[1] += "Seg fault";
            exitValue = process.exitValue();
        } catch (Exception e) {
            result[1] += e.getMessage();
            e.printStackTrace();
        }
        end = LocalTime.now();
        JSONObject obj = new JSONObject();
        String processedOutput = "";
        for(String line: result[0].split("\n")){
            if(!line.contains("make")){
                processedOutput += line + "\n";
            }
        }
        obj.put("stdout", processedOutput);
        obj.put("stderr", result[1]);
        obj.put("duration", Duration.between(start, end).toMillis());
        obj.put("exitvalue", exitValue);
        return obj;
    }
}

class Input {
    public final ArrayList<Long> moduleInputs;

    public Input(String path) throws InputException {
        ArrayList<Long> map = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try {
            obj = jsonParser.parse(new FileReader(path + "Input.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray inputs = (JSONArray) jsonObject.get("Input");
        for (int i = 0; i < inputs.size(); i++) {
            map.add((Long) inputs.get(i));
        }

        moduleInputs = map;
    }

    class InputException extends Exception {
        public final ModuleConfig errorModule;

        public InputException(ModuleConfig moduleConfig) {
            errorModule = moduleConfig;
        }

        @Override
        public String toString() {
            return "Incorrect input for module: " + errorModule.moduleName;
        }
    }
}

class ProcessMonitor extends Thread {
    private Process module;
    private LocalTime start;
    public boolean infiniteLoop = false;

    public ProcessMonitor(Process module, LocalTime start) {
        this.module = module;
        this.start = start;
    }

    @Override
    public void run() {
        while (module.isAlive()) {
            LocalTime temp = LocalTime.now();
            if (Math.abs(Duration.between(temp, start).toMillis()) > 5000) {
                module.destroyForcibly();
                infiniteLoop = true;
                break;
            }
        }
    }
}

class ModuleRunner {
    public final ModuleConfig moduleConfig;
    public final String input;
    public JSONObject result = null;
    public final String path;

    public ModuleRunner(ModuleConfig moduleConfig, String input, String path) {
        this.moduleConfig = moduleConfig;
        this.input = input;
        this.path = path;
    }

    // @Override
    public void run() {
        result = moduleConfig.executeModule(input, path);
    }

    @SuppressWarnings("unchecked")
    public JSONObject getResults() {
        result.put("moduleName", moduleConfig.moduleName);
        return result;
    }
}