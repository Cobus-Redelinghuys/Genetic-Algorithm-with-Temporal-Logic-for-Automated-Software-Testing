import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class GeneticAlgorithm {
    HashSet<Chromosome> population = new HashSet<>();
    Stats stats = new Stats();

    GeneticAlgorithm() {
        while (population.size() < Config.populationSize) {
            population.add(new Chromosome());
        }
    }

    public void run() {
        LocalDateTime start = LocalDateTime.now();
        for (int i = 0; i < Config.numGenerations; i++) {
            LocalDateTime generationStartTime = LocalDateTime.now();
            SelectionResult selectionResult = Config.selectionMethod.selectChromosomes(population, i);
            if (selectionResult.winners.length == 0) {
                Chromosome[] popArr = population.toArray(new Chromosome[0]);
                for (int j = 0; j < Config.tournamentSize; j++) {
                    population.remove(popArr[j]);
                    population.add(new Chromosome());
                }

            } else {
                ArrayList<Chromosome> offspring = new ArrayList<>();
                for (int j = 1; j < selectionResult.winners.length; j++) {
                    float prob = Config.random.nextFloat();
                    if (prob < Config.crossoverProp) {
                        Chromosome[] crossedOver = Config.crossOver.crossOver(selectionResult.winners[j - 1],
                                selectionResult.winners[j]);
                        prob = Config.random.nextFloat();
                        if (prob < Config.mutationProp) {
                            crossedOver[0] = Config.mutation.mutate(crossedOver[0]);
                        }
                        prob = Config.random.nextFloat();
                        if (prob < Config.mutationProp) {
                            crossedOver[1] = Config.mutation.mutate(crossedOver[1]);
                        }

                        for (Chromosome chromosome : crossedOver) {
                            offspring.add(chromosome);
                        }

                    } else {
                        prob = Config.random.nextFloat();
                        if (prob < Config.mutationProp) {
                            offspring.add(Config.mutation.mutate(selectionResult.winners[j]));
                        }
                    }
                    prob = Config.random.nextFloat();
                        if (prob < Config.mutationProp) {
                            offspring.add(new Chromosome(selectionResult.winners[j]));
                        }
                }
                ArrayList<Chromosome> looserOptions = new ArrayList<>(Arrays.asList(selectionResult.loosers));
                for (int j=0; j < Config.tournamentSize; j++) {
                    if(looserOptions.size() == 0){
                        break;
                    }
                    Chromosome chromosome = looserOptions.get(Config.random.nextInt(looserOptions.size()));
                    if (offspring.size() > 0) {
                        population.remove(chromosome);
                        int replacement = Config.random.nextInt(offspring.size());
                        population.add(offspring.get(replacement));
                        offspring.remove(offspring.get(replacement));
                        looserOptions.remove(chromosome);
                    }
                }
            }
            ChromosomeDatabase.addGenerationInfo();
            LocalDateTime generationEndTime = LocalDateTime.now();
            stats.addStats(selectionResult.statsNode, population,
                    ChronoUnit.MILLIS.between(generationStartTime, generationEndTime));
            System.out.println(stats.getStatsForGeneration(i));
        }
        LocalDateTime endTime = LocalDateTime.now();
        stats.algorithmDuration = ChronoUnit.MILLIS.between(start, endTime);
    }

    void printSummaryToFile(String file) {
        stats.toJSONFile(file);
    }
}
