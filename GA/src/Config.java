import java.io.FileReader;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Config {
    static final GeneConfig[] genes;
    static final Random random;
    public static final double reproductionProp;
    public static final double crossoverProp;
    public static final int seed;
    public static final String mutationType;
    public static final int selectionSize;
    public static final int populationSize;
    public static final int numGenerations;
    public static final String crossOverType;
    public static final double mutationProp;
    public static final int nCrossOver;
    public static final int nMutation;
    public static final int tournamentSize;
    public static final int numContestants;
    public static final float LTLWeight;
    public static final float MWeight;
    public static final float GWeight;
    public static final int maxCrossOverAttempts;
    public static final int maxMutationAttempts;
    public static final float truncationSelectionPer;

    public static final CrossOver crossOver;
    public static final Mutation mutation;

    public static final Interpretor interpretor;

    public static final Selection selectionMethod;
    
    static {
        JSONParser jsonParser = new JSONParser();
        Object obj = null;
        try {
            obj = jsonParser.parse(new FileReader("Config.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        JSONObject jsonObject = (JSONObject) obj;
        JSONArray geneArray = (JSONArray) jsonObject.get("Genes");
        GeneConfig[] tempArr = new GeneConfig[geneArray.size()];
        for (int i = 0; i < tempArr.length; i++) {
            JSONObject jObject = (JSONObject) geneArray.get(i);
            tempArr[i] = GeneConfig.getGeneConfig(jObject);
        }
        genes = tempArr;
        reproductionProp = (double) jsonObject.get("reproductionProp");
        crossoverProp = (double) jsonObject.get("crossoverProp");
        seed = ((Long) jsonObject.get("seed")).intValue();
        mutationType = (String) jsonObject.get("mutationType");
        selectionSize = ((Long) jsonObject.get("selectionSize")).intValue();
        populationSize = ((Long) jsonObject.get("populationSize")).intValue();
        numGenerations = ((Long) jsonObject.get("numGenerations")).intValue();
        crossOverType = (String) jsonObject.get("crossOverType");
        mutationProp = (double) jsonObject.get("mutationProp");
        nCrossOver = ((Long) jsonObject.get("nCrossOver")).intValue();
        tournamentSize = ((Long) jsonObject.get("tournamentSize")).intValue();
        String interpreterPath = (String) jsonObject.get("interpreterPath");
        String interpreterCommand = (String) jsonObject.get("interpreterCommand");
        LTLWeight = ((Double) jsonObject.get("LTLWeight")).floatValue();
        MWeight = ((Double) jsonObject.get("MWeight")).floatValue();
        GWeight = ((Double) jsonObject.get("GWeight")).floatValue();
        random = new Random(seed);
        maxCrossOverAttempts = ((Long) jsonObject.get("maxCrossOverAttempts")).intValue();
        maxMutationAttempts = ((Long) jsonObject.get("maxMutationAttempts")).intValue();
        crossOver = CrossOver.getCrossOver(crossOverType);
        mutation = Mutation.getMutation(mutationType);
        nMutation = ((Long) jsonObject.get("nMutation")).intValue();
        int numberInterpreterInstances = ((Long) jsonObject.get("numberInterpreterInstances")).intValue();
        String interpretorExecutorName = (String) jsonObject.get("interpretorExecutorName");

        interpretor = new Interpretor(interpreterPath, interpreterCommand,
                numberInterpreterInstances, interpretorExecutorName);

        numContestants = ((Long) jsonObject.get("numberInterpreterInstances")).intValue();
        truncationSelectionPer = ((Double) jsonObject.get("truncationSelectionPer")).floatValue();
        String selectionName = (String)jsonObject.get("selectionMethod");
        selectionMethod = Selection.getSelection(selectionName);
    }
}

