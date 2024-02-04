import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Stats {
    final HashMap<Integer, StatsNode> fitnessSets = new HashMap<>();
    final HashMap<Integer, HashSet<Chromosome>> populationPerGeneration = new HashMap<>();
    final HashMap<Integer, Float> avgGen = new HashMap<>();
    final HashMap<Integer, Float> stdGen = new HashMap<>();
    final HashMap<Integer, Float> varGen = new HashMap<>();
    final HashMap<Integer, Float> bestGen = new HashMap<>();
    final HashMap<Integer, Chromosome> bestChromGen = new HashMap<>();
    final HashMap<Integer, Long> durGen = new HashMap<>();
    
    long algorithmDuration;



    public void addStats(StatsNode statsNode, HashSet<Chromosome> population, long generationDuration){
        fitnessSets.put(statsNode.generation, statsNode);
        populationPerGeneration.put(statsNode.generation, population);
        avgAndBest(statsNode.generation);
        std(statsNode.generation);
        var(statsNode.generation);
        durGen.put(statsNode.generation, generationDuration);
    }

    String getStatsForGeneration(int gen){
        String res = "Generation: " + gen + '\n';
        res += "Avg: " + avgGen.get(gen) + "\n";
        res += "Std: " + stdGen.get(gen) + "\n";
        res += "Var: " + varGen.get(gen) + "\n";
        res += "Best: " + bestGen.get(gen) + "\n";
        res += "Best: " + Arrays.toString(bestChromGen.get(gen).genes) + "\n";
        res += "Best: " + bestChromGen.get(gen).bits + "\n";
        res += "Dur: " + durGen.get(gen) + "\n";
        return res;
    }

    void avgAndBest(int gen){
        StatsNode sn = fitnessSets.get(gen);
        Float best = Float.NEGATIVE_INFINITY;
        Chromosome bestChrom = null;
        float sum = 0f;
        for (Chromosome chromosome : sn.fitnessSet.keySet()) {
            float fitness = sn.fitnessSet.get(chromosome);
            if(fitness > best){
                best = fitness;
                bestChrom = chromosome;
            }
            sum += fitness;
        }

        avgGen.put(gen, sum/sn.fitnessSet.size());
        bestGen.put(gen, best);
        bestChromGen.put(gen, bestChrom);
    }

    void std(int gen){
        StatsNode sn = fitnessSets.get(gen);
        float avg = avgGen.get(gen);
        float sum = 0f;
        for (Chromosome chromosome : sn.fitnessSet.keySet()) {
            float fitness = sn.fitnessSet.get(chromosome);
            sum += Math.pow(fitness-avg, 2);            
        }

        stdGen.put(gen, (float)Math.sqrt(sum/sn.fitnessSet.size()));
    }

    void var(int gen){
        HashSet<String> representations = new HashSet<>();
        Chromosome[] pop = populationPerGeneration.get(gen).toArray(new Chromosome[0]);
        for (Chromosome chromosome : pop) {
            representations.add(chromosome.bits);
        }
        varGen.put(gen, (float)representations.size() / pop.length);
    }

    @SuppressWarnings("unchecked")
    JSONObject toJSON(){
        JSONObject result = new JSONObject();
        JSONArray fitRes = new JSONArray();
        for (StatsNode sNs : fitnessSets.values()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Detailed", sNs.toJSON());
            jsonObject.put("Summary", toJSONSummary(sNs.generation));
            fitRes.add(jsonObject);
        }
        result.put("Results", fitRes);
        return result;
    }

    @SuppressWarnings("unchecked")
    JSONObject toJSONSummary(int gen){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("avg", avgGen.get(gen));
        jsonObject.put("std", stdGen.get(gen));
        jsonObject.put("var", varGen.get(gen));
        jsonObject.put("bestFitness", bestGen.get(gen));
        JSONArray genes = new JSONArray();
        for(Integer gene: bestChromGen.get(gen).genes){
            genes.add(gene);
        }
        jsonObject.put("bestGenes", genes);
        jsonObject.put("bestBinary", bestChromGen.get(gen).bits);
        jsonObject.put("duration", durGen.get(gen));
        return jsonObject;
    }

    void toJSONFile(String fileName){
        JSONObject jsonObject = toJSON();
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(jsonObject.toJSONString());
            System.out.println("JSON object has been written to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class StatsNode{
    final int generation;
    final HashMap<Chromosome, Float> fitnessSet;
    final HashMap<Chromosome, InterpretorResults[]> interpretorResults;

    StatsNode(int generation, HashMap<Chromosome, Float> fitnessSet, HashMap<Chromosome, InterpretorResults[]> interpretorResults){
        this.generation = generation;
        this.fitnessSet = fitnessSet;
        this.interpretorResults = interpretorResults;
    }

    @SuppressWarnings("unchecked")
    JSONObject toJSON(){
        JSONObject result = new JSONObject();
        result.put("generation", generation);
        JSONArray chromosomeArr = new JSONArray();
        for (Chromosome chromosome : fitnessSet.keySet()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bits", chromosome.bits);
            JSONArray genes = new JSONArray();
            for (Integer gene : chromosome.genes) {
                genes.add(gene);
            }
            jsonObject.put("genes", genes);
            jsonObject.put("fitness", fitnessSet.get(chromosome));
            jsonObject.put("fitnessBreakdown", ChromosomeDatabase.getDB(chromosome, generation).toJSON());
            JSONArray irResults = new JSONArray();
            for (InterpretorResults irResult : interpretorResults.get(chromosome)) {
                irResults.add(irResult.toJSON());
            }
            jsonObject.put("InterpretorResults", irResults);
            chromosomeArr.add(jsonObject);
        }
        result.put("Chromosomes", chromosomeArr);
        return result;
    }
}
