import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class FitnessConfig {
    public static final FitnessConfigField Safety;
    public static final FitnessConfigField Livelyness;
    public static final FitnessConfigField SegFault;
    public static final FitnessConfigField Exceptions;
    public static final ExecutionTimeField ExecutionTime;
    public static final IllegalOutputField IllegalOutput;
    public static final ExpectedOutputField ExpectedOutput;

    public static final float weightsOfActiveProperties;

    static {
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        float tempActive = 0;
        try {
            obj = ((JSONObject) jsonParser.parse(new FileReader("Config.json"))).get("FitnessFunction");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = (JSONObject) obj;
        Object res = null;
        try {
            res = new FitnessConfigField((JSONObject) ((JSONObject) jsonObject).get("Safety"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new FitnessConfigField();
        } finally {
            Safety = (FitnessConfigField) res;
            if(Safety.enabled){
                tempActive += Safety.weight;
            }
        }

        try {
            res = new FitnessConfigField((JSONObject) ((JSONObject) jsonObject).get("Livelyness"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new FitnessConfigField();
        } finally {
            Livelyness = (FitnessConfigField) res;
            if(Livelyness.enabled){
                tempActive += Livelyness.weight;
            }
        }

        try {
            res = new FitnessConfigField((JSONObject) ((JSONObject) jsonObject).get("SegFault"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new FitnessConfigField();
        } finally {
            SegFault = (FitnessConfigField) res;
            if(SegFault.enabled){
                tempActive += SegFault.weight;
            }
        }

        try {
            res = new FitnessConfigField((JSONObject) ((JSONObject) jsonObject).get("Exceptions"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new FitnessConfigField();
        } finally {
            Exceptions = (FitnessConfigField) res;
            if(Exceptions.enabled){
                tempActive += Exceptions.weight;
            }
        }

        try {
            res = new ExecutionTimeField((JSONObject) ((JSONObject) jsonObject).get("ExecutionTime"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new ExecutionTimeField();
        } finally {
            ExecutionTime = (ExecutionTimeField) res;
            if(ExecutionTime.enabled){
                tempActive += ExecutionTime.weight;
            }
        }

        try {
            res = new IllegalOutputField((JSONObject) jsonObject.get("IllegalOutput"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new IllegalOutputField();
        } finally {
            IllegalOutput = (IllegalOutputField) res;
            if(IllegalOutput.enabled){
                tempActive += IllegalOutput.weight;
            }
        }

        try {
            res = new ExpectedOutputField((JSONObject) jsonObject.get("ExpectedOutput"));
        } catch (Exception e) {
            e.printStackTrace();
            res = new ExpectedOutputField();
        } finally {
            ExpectedOutput = (ExpectedOutputField) res;
            if(ExpectedOutput.enabled){
                tempActive+= ExpectedOutput.weight;
            }
        }
        weightsOfActiveProperties = tempActive;
    }

}

class FitnessConfigField {
    public final boolean enabled;
    public final float weight;

    public FitnessConfigField(JSONObject jsonObject) throws RuntimeException {
        try {
            enabled = Boolean.parseBoolean(jsonObject.get("enabled").toString());
            weight =  Float.parseFloat(jsonObject.get("weight").toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Malformed safety property");
        }
    }

    public FitnessConfigField() {
        enabled = false;
        weight = Float.NaN;
    }
}

class IllegalOutputField {
    public final boolean enabled;
    public final float weight;
    public final String[] words;

    public IllegalOutputField(JSONObject jsonObject) throws RuntimeException {
        try {
            enabled = Boolean.parseBoolean(jsonObject.get("enabled").toString());
            weight = Float.parseFloat(jsonObject.get("weight").toString());
            JSONArray arr = (JSONArray) jsonObject.get("words");
            String[] res = new String[arr.size()];
            for (int i = 0; i < res.length; i++) {
                res[i] = (String) arr.get(i);
            }
            words = res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Malformed safety property");
        }
    }

    public IllegalOutputField() {
        enabled = false;
        weight = Float.NaN;
        words = new String[0];
    }
}

class ExecutionTimeField {
    public final boolean enabled;
    public final float weight;
    public final long maxTime;

    public ExecutionTimeField(JSONObject jsonObject) throws RuntimeException {
        try {
            enabled = Boolean.parseBoolean(jsonObject.get("enabled").toString());
            weight = Float.parseFloat(jsonObject.get("weight").toString());
            maxTime = Long.parseLong(jsonObject.get("maxTime").toString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Malformed fitness function");
        }
    }

    public ExecutionTimeField() {
        enabled = false;
        weight = Float.NaN;
        maxTime = 0;
    }

}

class ExpectedOutputField {
    public final boolean enabled;
    public final float weight;
    public final boolean exactMatch;
    public final String splitChar;

    public ExpectedOutputField(JSONObject jsonObject) throws RuntimeException {
        try {
            enabled = Boolean.parseBoolean(jsonObject.get("enabled").toString());
            weight = Float.parseFloat(jsonObject.get("weight").toString());
            exactMatch = Boolean.parseBoolean(jsonObject.get("exactMatch").toString());
            splitChar = jsonObject.get("splittingChar").toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Malformed safety property");
        }
    }

    public ExpectedOutputField() {
        enabled = false;
        weight = 0;
        exactMatch = false;
        splitChar = "\n";
    }

    public float constantExpected(InterpretorResults output) {
        /*int matched = 0;
        int possibles = 0;
        int penalty = 0;
        if (exactMatch) {
            if (output.studentStdOut.equals(output.instructorStdOut)) {
                return 0;
            }
            return 0;
        } else {
            ArrayList<String> studentLines = new ArrayList<>();
            Collections.addAll(studentLines, output.studentStdOut.split(splitChar));
            ArrayList<String> instructorLines = new ArrayList<>();
            Collections.addAll(instructorLines, output.instructorStdOut.split(splitChar));
            possibles = instructorLines.size();
            penalty = studentLines.size();
            while (!instructorLines.isEmpty() && !studentLines.isEmpty()) {
                String studentLine = studentLines.get(0);
                if (instructorLines.contains(studentLine)) {
                    matched++;
                }
                studentLines.remove(studentLine);
                instructorLines.remove(studentLine);
            }
        }

        float result = Math.abs(((float) matched - (float) penalty)) / (float) possibles;
        return result;*/
        return 0;
    }

}
