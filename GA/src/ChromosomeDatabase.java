import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class ChromosomeDatabase {
    static HashMap<Integer, ArrayList<Chromosome>>[] db;
    static HashMap<Integer, HashMap<Chromosome, ChromosomeDBInfo>> chromosomeDBInfo = new HashMap<>();

    static HashMap<Integer, HashMap<Chromosome, ChromosomeDBInfo>> genChromosomeDBInfo = new HashMap<>();

    public static void addGenerationInfo(){
        chromosomeDBInfo.putAll(genChromosomeDBInfo);
        for (Integer gen : genChromosomeDBInfo.keySet()) {
            for (Chromosome chromosome : genChromosomeDBInfo.get(gen).keySet()) {
                addChromosome(chromosome);
            }
        }
        genChromosomeDBInfo = new HashMap<>();
    }

    static int numChromosomesEvaluated(){
        int count =0;
        for (int gen : chromosomeDBInfo.keySet()) {
            count += chromosomeDBInfo.get(gen).size();
        }
        return count;
    }

    static {
        db = new HashMap[Config.genes.length];
        for (int i = 0; i < db.length; i++) {
            db[i] = new HashMap<>();
        }

    }

    static void addChromosome(Chromosome chromosome) {
        for (int i = 0; i < Config.genes.length; i++) {
            if (!db[i].containsKey(chromosome.genes[i])) {
                db[i].put(chromosome.genes[i], new ArrayList<>());
            }
            db[i].get(chromosome.genes[i]).add(chromosome);
        }
    }

    static void addDBInfo(ChromosomeDBInfo cDBInfo) {
        if (!genChromosomeDBInfo.containsKey(cDBInfo.gen)) {
            genChromosomeDBInfo.put(cDBInfo.gen, new HashMap<>());
        }

        if (!genChromosomeDBInfo.get(cDBInfo.gen).containsKey(cDBInfo.chromosome)) {
            genChromosomeDBInfo.get(cDBInfo.gen).put(cDBInfo.chromosome, cDBInfo);
        } else {
            genChromosomeDBInfo.get(cDBInfo.gen).put(cDBInfo.chromosome, cDBInfo);
        }
    }

    static ChromosomeDBInfo get(Chromosome chromosome, int gen) {
        return genChromosomeDBInfo.get(gen).get(chromosome);
    }

    static ChromosomeDBInfo getDB(Chromosome chromosome, int gen){
        return chromosomeDBInfo.get(gen).get(chromosome);
    }

    static float G(Chromosome chromosome, int gen) {
        ChromosomeDBInfo chromosomeDBInfo = get(chromosome, gen);
        chromosomeDBInfo.gSubValues = new float[db.length];
        float sum = 0;
        for (int i = 0; i < db.length; i++) {
            if (db[i].containsKey(chromosome.genes[i])) {
                int size = db[i].get(chromosome.genes[i]).size();
                int tGen = gen+1;
                sum += (float) size / (float) numChromosomesEvaluated();
                chromosomeDBInfo.gSubValues[i] = (float) size / (float) numChromosomesEvaluated();
            } else {
                sum += 0;
                chromosomeDBInfo.gSubValues[i] = 0;
            }
        }
        addDBInfo(chromosomeDBInfo);
        return (sum / chromosome.genes.length) * Config.GWeight;
    }

    static JSONObject dbDump() {
        JSONObject result = new JSONObject();
        for (Integer gen : chromosomeDBInfo.keySet()) {
            JSONArray genInfo = new JSONArray();
            for (Chromosome chromosome : chromosomeDBInfo.get(gen).keySet()) {
                JSONObject jsonObject = new JSONObject();
                JSONObject dbInfo = chromosomeDBInfo.get(gen).get(chromosome).toJSON();
                jsonObject.put("FitnessInfo", dbInfo);
                jsonObject.put("ChromosomInfo", chromosome.toJSON());
                genInfo.add(jsonObject);
            }
            result.put(gen, genInfo);
        }
        return result;
    }

    static void printToFile(String fileName) {
        JSONObject jsonObject = dbDump();
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonObject.toJSONString());
            System.out.println("JSON object has been written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class ChromosomeDBInfo {
    public Chromosome chromosome;
    public float Safety;
    public float Livelyness;
    public float SegFault;
    public float Exceptions;
    public float ExecutionTime;
    public float IllegalOutput;
    public float ExpectedOutput;
    public float m = 0;
    public float g = 0;
    public float ltl = 0;
    public float[] gSubValues;
    public int gen;

    ChromosomeDBInfo(Chromosome chromosome, int gen) {
        this.chromosome = chromosome;
        this.gen = gen;
        gSubValues = new float[chromosome.genes.length];
        for(int i=0; i < chromosome.genes.length; i++){
            gSubValues[i] = 0;
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("SegFault", SegFault);
        jsonObject.put("Safety", Safety);
        jsonObject.put("Livelyness", Livelyness);
        jsonObject.put("Exceptions", Exceptions);
        jsonObject.put("ExecutionTime", ExecutionTime);
        jsonObject.put("IllegalOutput", IllegalOutput);
        jsonObject.put("ExpectedOutput", ExpectedOutput);
        jsonObject.put("m", m);
        jsonObject.put("g", g);
        jsonObject.put("ltl", ltl);
        JSONArray gArray = new JSONArray();
        for (Float gVal : gSubValues) {
            gArray.add(gVal);
        }
        jsonObject.put("gSubValues", gArray);

        return jsonObject;
    }
}