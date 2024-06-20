abstract public class CrossOver {
    abstract Chromosome[] crossOver(Chromosome chromosomeA, Chromosome chromosomeB);

    static CrossOver getCrossOver(String type) throws RuntimeException {
        if (type.equals("OnePointCrossOver")) {
            return new OnePointCrossover();
        }
        if(type.equals("NPointCrossOver")){
            return new NPointCrossover();
        }
        if(type.equals("SegmentedCrossOver")){
            return new SegmentedCrossover();
        }
        if(type.equals("UniformedCrossOver")){
            return new UniformCrossover();
        }
        throw new RuntimeException("Invalid CrossOverType");
    }
}

class OnePointCrossover extends CrossOver {
    @Override
    Chromosome[] crossOver(Chromosome chromosomeA, Chromosome chromosomeB) {
        Chromosome[] result = new Chromosome[2];
        String chromosomeABits = chromosomeA.bits;
        String chromosomeBBits = chromosomeB.bits;
        for (int i = 0; i < Config.maxCrossOverAttempts; i++) {
            int crossOverPoint = Config.random.nextInt(chromosomeABits.length());
            String nOffSpringA = chromosomeABits.substring(0, crossOverPoint)
                    + chromosomeBBits.substring(crossOverPoint);
            String nOffSpringB = chromosomeBBits.substring(0, crossOverPoint)
                    + chromosomeABits.substring(crossOverPoint);
            result[0] = new Chromosome(nOffSpringA);
            result[1] = new Chromosome(nOffSpringB);
            if (!result[0].isValid() || !result[1].isValid()) {
                result[0] = null;
                result[1] = null;
            } else {
                return result;
            }
        }

        result[0] = new Chromosome(chromosomeA);
        result[1] = new Chromosome(chromosomeB);
        return result;
    }
}

class NPointCrossover extends CrossOver {
    @Override
    Chromosome[] crossOver(Chromosome chromosomeA, Chromosome chromosomeB) {
        Chromosome[] result = new Chromosome[2];
        result[0] = new Chromosome(chromosomeA);
        result[1] = new Chromosome(chromosomeB);
        CrossOver onePointCrossOver = new OnePointCrossover();
        for (int i = 0; i < Config.maxCrossOverAttempts; i++) {
            for (int j = 0; j < Config.nCrossOver; j++) {
                result = onePointCrossOver.crossOver(result[0], result[1]);
            }
            if (result[0].isValid() && result[1].isValid()) {
                return result;
            } else {
                result[0] = new Chromosome(chromosomeA);
                result[1] = new Chromosome(chromosomeB);
            }
        }

        result[0] = new Chromosome(chromosomeA);
        result[1] = new Chromosome(chromosomeB);
        return result;
    }
}

class SegmentedCrossover extends CrossOver {
    @Override
    Chromosome[] crossOver(Chromosome chromosomeA, Chromosome chromosomeB) {
        Chromosome[] result = new Chromosome[2];
        result[0] = new Chromosome(chromosomeA);
        result[1] = new Chromosome(chromosomeB);
        CrossOver onePointCrossOver = new OnePointCrossover();
        int n = Config.random.nextInt(result[0].bits.length());
        for (int i = 0; i < Config.maxCrossOverAttempts; i++) {
            for (int j = 0; j < n; j++) {
                result = onePointCrossOver.crossOver(result[0], result[1]);
            }
            if (result[0].isValid() && result[1].isValid()) {
                return result;
            } else {
                result[0] = new Chromosome(chromosomeA);
                result[1] = new Chromosome(chromosomeB);
            }
        }

        result[0] = new Chromosome(chromosomeA);
        result[1] = new Chromosome(chromosomeB);
        return result;
    }
}

class UniformCrossover extends CrossOver{
    @Override
    Chromosome[] crossOver(Chromosome chromosomeA, Chromosome chromosomeB) {
        Chromosome[] result = new Chromosome[2];
        String chromosomeABits = chromosomeA.bits;
        String chromosomeBBits = chromosomeB.bits;
        for (int i = 0; i < Config.maxCrossOverAttempts; i++) {
            String nOffSpringA = "";
            String nOffSpringB = "";
            for(int j=0; j < chromosomeABits.length(); j++){
                if(Config.random.nextFloat() <= 0.5f){
                    nOffSpringA += chromosomeBBits.charAt(j);
                    nOffSpringB += chromosomeABits.charAt(j);
                } else {
                    nOffSpringA += chromosomeABits.charAt(j);
                    nOffSpringB += chromosomeBBits.charAt(j);
                }
            }
            result[0] = new Chromosome(nOffSpringA);
            result[1] = new Chromosome(nOffSpringB);
            if (!result[0].isValid() || !result[1].isValid()) {
                result[0] = null;
                result[1] = null;
            } else {
                return result;
            }
        }

        result[0] = new Chromosome(chromosomeA);
        result[1] = new Chromosome(chromosomeB);
        return result;
    }
}