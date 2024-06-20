abstract public class Mutation {
    abstract Chromosome mutate(Chromosome chromosome);

    static Mutation getMutation(String type) throws RuntimeException {
        if (type.equals("SingleBitInversion")) {
            return new SingleBitInversion();
        }
        if(type.equals("BitWiseInversion")){
            return new BitWiseInversion();
        }
        if(type.equals("RandomInversion")){
            return new RandomInversion();
        }
        throw new RuntimeException("Invalid CrossOverType");
    }
}

class SingleBitInversion extends Mutation {
    @Override
    Chromosome mutate(Chromosome chromosome) {
        for (int i = 0; i < Config.maxMutationAttempts; i++) {
            String bits = chromosome.bits;
            int point = Config.random.nextInt(bits.length() - 2) + 1;
            String a = bits.substring(0, point);
            String b = bits.substring(point + 1);
            char bit = bits.charAt(point);
            if (bit == '1') {
                bit = '0';
            } else {
                bit = '1';
            }
            String res = a + bit + b;
            Chromosome result = new Chromosome(res);
            if(result.isValid()){
                return result;
            }
        }
        return new Chromosome(chromosome);
    }
}

class BitWiseInversion extends Mutation {
    @Override
    Chromosome mutate(Chromosome chromosome) {
        for (int i = 0; i < Config.maxMutationAttempts; i++) {
            String resBits = "";
            for (int j = 0; j < chromosome.bits.length(); j++) {
                if(chromosome.bits.charAt(j) == '1'){
                    resBits += '0';
                } else {
                    resBits += '1';
                }
            }
            Chromosome result = new Chromosome(resBits);
            if(result.isValid()){
                return result;
            }
        }
        return new Chromosome(chromosome);
    }
}

class RandomInversion extends Mutation{
    @Override
    Chromosome mutate(Chromosome chromosome) {
        for (int i = 0; i < Config.maxMutationAttempts; i++) {
            Chromosome result = chromosome;
            Mutation singlePointMutation = new SingleBitInversion();
            for(int j=0; j < Config.nMutation; j++){
                result = singlePointMutation.mutate(result);
            }
            if(result.isValid()){
                return result;
            }
        }
        return new Chromosome(chromosome);
    }
}