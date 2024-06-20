import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class App {
    public static void main(String[] args) throws Exception {
        args = new String[] { "./" };
        ModulesConfig.set(args[0]);
        Input input = null;
        try {
            input = new Input(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONArray instructorResult = SUT(input, args[0]);
        System.out.println("Finished with SUT");
        JSONObject result = new JSONObject();
        JSONArray moduleResults = new JSONArray();
        HashMap<String, JSONObject> instructionMap = new HashMap<>();
        for (Object object : instructorResult) {
            JSONObject jsonObject = (JSONObject) object;
            instructionMap.put((String) jsonObject.get("moduleName"), jsonObject);
        }
        String moduleName = "BasicTask";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("stdOut", instructionMap.get(moduleName).get("stdout"));
        jsonObject.put("errOut", instructionMap.get(moduleName).get("stderr"));
        jsonObject.put("exeTime", instructionMap.get(moduleName).get("duration"));
        jsonObject.put("exitCode", instructionMap.get(moduleName).get("exitvalue"));
        moduleResults.add(jsonObject);

        result.put("results", moduleResults);

        System.out.println("Finished creating json object");
        try (FileWriter file = new FileWriter(args[0] + "/Output.json");) {
            file.write(result.toJSONString());
            file.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static JSONArray SUT(Input input, String path) {
        JSONArray result = ModulesConfig.executeSystem(input, path + "./SUT");

        try (FileWriter file = new FileWriter(path + "/SUT.json");) {
            file.write(result.toJSONString());
            file.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("SUT");
        for (int i = 0; i < result.size(); i++) {
            JSONObject res = (JSONObject) result.get(i);
            System.out.println("Module: " + i);
            if (((String) res.get("stdout")).contains("\n"))
                System.out.print("stdout: " + res.get("stdout"));
            else
                System.out.println("stdout: " + res.get("stdout"));
            if (((String) res.get("stderr")).contains("\n"))
                System.out.print("stderr: " + res.get("stderr"));
            else
                System.out.println("stderr: " + res.get("stderr"));
            System.out.println("duration: " + res.get("duration"));
            System.out.println("exit value: " + res.get("exitvalue"));
        }
        return result;
    }
}
