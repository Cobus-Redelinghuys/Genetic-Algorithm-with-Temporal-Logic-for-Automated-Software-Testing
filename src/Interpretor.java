import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Interpretor {
    public final String interpreterPath;
    public final String interpreterCommand;
    public final String interpretorExecutorName;
    public final int numberInterpreterInstances;
    public final File[] interpreterInstacePaths;
    public final AtomicBoolean[] finished;

    Interpretor(String path, String command, int nInstances, String executor)
            throws RuntimeException {
        interpreterPath = path;
        interpreterCommand = command;
        numberInterpreterInstances = nInstances;
        interpretorExecutorName = executor;
        interpreterInstacePaths = new File[numberInterpreterInstances];
        finished = new AtomicBoolean[numberInterpreterInstances];
        File interpretorSourceFile = new File(interpreterPath);
        File parentInstanceDir = new File("./InterpretorInstances");
        if (parentInstanceDir.exists()) {
            try {
                Files
                        .walk(parentInstanceDir.toPath()) // Traverse the file tree in depth-first order
                        .sorted(Comparator.reverseOrder())
                        .forEach(parentPath -> {
                            try {
                                // System.out.println("Deleting: " + parentPath);
                                Files.delete(parentPath); // delete each file or directory
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parentInstanceDir.mkdir();
        for (int i = 0; i < interpreterInstacePaths.length; i++) {
            interpreterInstacePaths[i] = new File(parentInstanceDir.getPath(), "Instance_" + i);
            interpreterInstacePaths[i].mkdir();
            finished[i] = new AtomicBoolean(false);
            try {
                for (File file : interpretorSourceFile.listFiles()) {
                    Path source = file.toPath();
                    Path dest = Paths.get(interpreterInstacePaths[i].toPath().toString() + "/" + file.getName());
                    Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
                    if (file.isDirectory()) {
                        recDirectories(source, dest);
                    }
                }
                finished[i].set(true);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        
    }

    private void recDirectories(Path parentSource, Path parentDir) throws Exception {
        for (File file : parentSource.toFile().listFiles()) {
            Path source = file.toPath();
            Path dest = Paths.get(parentDir.toString() + "/" + file.getName());
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
            if (file.isDirectory()) {
                recDirectories(source, dest);
            }
        }
    }

    public HashMap<Chromosome, InterpretorResults[]> run(Chromosome[] chromosomes) {

        ArrayList<InterpretorInstance> interpreterInstancesQueue = new ArrayList<>();
        ArrayList<InterpretorInstance> finishedList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);
        for (Chromosome chromosome : chromosomes) {
            interpreterInstancesQueue
                    .add(new InterpretorInstance(chromosome, interpreterCommand,
                            interpretorExecutorName));
        }
        InterpretorInstance[] instances = new InterpretorInstance[numberInterpreterInstances];
        while (!interpreterInstancesQueue.isEmpty()) {
            for (int i = 0; i < numberInterpreterInstances; i++) {
                if (instances[i] == null) {
                    if (interpreterInstancesQueue.size() > 0) {
                        instances[i] = interpreterInstancesQueue.remove(0);
                        instances[i].instanceNumber = i;
                        instances[i].start();
                    }
                } else if (instances[i].done.get()) {
                    try {
                        instances[i].join();
                        //System.out.println("Chromosome done: " + (count.getAndIncrement()+1) + "/" + chromosomes.length);
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finishedList.add(instances[i]);
                    instances[i] = null;
                }
            }
        }

        HashMap<Chromosome, InterpretorResults[]> results = new HashMap<>();
        for (InterpretorInstance interpretorInstance : instances) {
            while (interpretorInstance != null && !interpretorInstance.done.get()) {

            }
            if (interpretorInstance != null) {
                finishedList.add(interpretorInstance);
                //System.out.println("Chromosome done: " + (count.getAndIncrement()+1) + "/" + chromosomes.length);
            }
        }

        for (InterpretorInstance interpretorInstance : finishedList) {
            JSONArray resultsArray = (JSONArray) interpretorInstance.result.get("results");
            InterpretorResults[] interpretorResultsArray = new InterpretorResults[resultsArray.size()];
            int i = 0;
            for (Object obj : resultsArray) {
                JSONObject jsonObject = (JSONObject) obj;
                interpretorResultsArray[i] = new InterpretorResults(jsonObject);
                i++;
            }
            results.put(interpretorInstance.chromosome, interpretorResultsArray);
        }

        return results;
    }

}

class InterpretorInstance extends Thread {
    final Chromosome chromosome;
    int instanceNumber;
    final String command;
    final String executor;
    JSONObject result;
    AtomicBoolean done = new AtomicBoolean(false);

    InterpretorInstance(Chromosome chromosome, String command, String executor) {
        this.chromosome = chromosome;
        this.command = command;
        this.executor = executor;
    }

    //@Override
    public void run() {
        File file = new File("InterpretorInstances/Instance_" + instanceNumber + "/Input.json");
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(chromosome.toJSONString());
            fileWriter.close();
            String cmd = command + "InterpretorInstances/Instance_" + instanceNumber + "/" + executor; 
            String params = "InterpretorInstances/Instance_" + instanceNumber + "/";
            runProgram(cmd, params);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
            done.set(true);
            return;
        } finally {
            JSONParser jsonParser = new JSONParser();
            Object obj = null;
            try {
                obj = jsonParser
                        .parse(new FileReader("InterpretorInstances/Instance_" + instanceNumber + "/Output.json"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            result = (JSONObject) obj;
            /*for(Object objt : (JSONArray)result.get("results")){
                JSONObject jobjt = (JSONObject)objt;
                if(!((String)jobjt.get("instructorErrOut")).isEmpty()){
                    System.out.println(chromosome.geneString());
                    System.out.println((String)jobjt.get("instructorErrOut"));
                }
            }*/
            done.set(true);
        }
    }

    public static void runProgram(String str, String path) throws Exception {

        //System.out.println(str +" " +path);
        Process pro = Runtime.getRuntime().exec(str +" " +path);
        //System.out.println(str +" " +path);
        String error = "";
        // System.out.println(str + " stdout:" + pro.getInputStream());
        // System.out.println(str + " stderr:" + pro.getErrorStream());
        /*LocalDateTime start = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now();
        while(ChronoUnit.MILLIS.between(start, endTime) <= 120000){
            endTime = LocalDateTime.now();
            //System.out.println(ChronoUnit.MILLIS.between(start, endTime));
        }
        System.out.println(LocalDateTime.now());
        ChronoUnit.MILLIS.between(start, endTime);
        pro.destroy();*/

        int input = pro.getErrorStream().read();
        while (input != -1) {
            error += (char) input;
            input = pro.getErrorStream().read();
        }
        // System.out.println(error);
        input = pro.getInputStream().read();
        while (input != -1) {
            error += (char) input;
            input = pro.getInputStream().read();
        }
         //System.out.println(error);

        pro.waitFor();

        // System.out.println(str + " exitValue() " + pro.exitValue());
    }

}

class InterpretorResults {
    final String studentStdOut;
    final String studentErrOut;
    final String instructorStdOut;
    final String instructorErrOut;
    final long studentExeTime;
    final long instructorExeTime;
    final int studentExitCode;
    final int instructorExitCode;

    InterpretorResults(JSONObject output) {
        studentStdOut = (String) output.get("studentStdOut");
        studentErrOut = (String) output.get("studentErrOut");
        instructorStdOut = (String) output.get("instructorStdOut");
        instructorErrOut = (String) output.get("instructorErrOut");
        studentExeTime = (Long) output.get("studentExeTime");
        instructorExeTime = (Long) output.get("instructorExeTime");
        studentExitCode = ((Long) output.get("studentExitCode")).intValue();
        instructorExitCode = ((Long) output.get("instructorExitCode")).intValue();
    }

    @SuppressWarnings("unchecked")
    JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("studentStdOut", studentStdOut);
        jsonObject.put("studentErrOut", studentErrOut);
        jsonObject.put("instructorStdOut", instructorStdOut);
        jsonObject.put("instructorErrOut", instructorErrOut);
        jsonObject.put("studentExeTime", studentExeTime);
        jsonObject.put("instructorExeTime", instructorExeTime);
        jsonObject.put("studentExitCode", studentExitCode);
        jsonObject.put("instructorExitCode", instructorExitCode);
        return jsonObject;
    }
}