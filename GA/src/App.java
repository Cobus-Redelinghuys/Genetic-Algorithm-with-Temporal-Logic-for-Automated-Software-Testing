public class App {
    public static void main(String[] args) throws Exception {
        while(true){
            int count = 0;
            for (int i = 0; i < Config.interpretor.finished.length; i++) {
                if(Config.interpretor.finished[i] != null && Config.interpretor.finished[i].get() == true){
                    count++;
                }
            }
            if(count == Config.interpretor.interpreterInstacePaths.length){
                break;
            }
        }
        GeneticAlgorithm ga = new GeneticAlgorithm();
        ga.run();
        ga.printSummaryToFile("results.json");
        ChromosomeDatabase.printToFile("database.json");
        InterpretorInstance.runProgram("python3 visualizer.py", "");
    }
}
