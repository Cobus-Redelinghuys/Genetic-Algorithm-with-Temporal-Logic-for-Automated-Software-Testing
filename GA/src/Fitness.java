public class Fitness {
    public static float determineFitness(InterpretorResults[] output, int gen, Chromosome chromosome) {
        ChromosomeDBInfo chromosomeDBInfo = new ChromosomeDBInfo(chromosome, gen);
        ChromosomeDatabase.addDBInfo(chromosomeDBInfo);
        float ltl = LTL(output, chromosome, gen);
        chromosomeDBInfo = ChromosomeDatabase.get(chromosome, gen);
        chromosomeDBInfo.ltl = ltl;
        ChromosomeDatabase.addDBInfo(chromosomeDBInfo);
        float m = M(output);
        chromosomeDBInfo = ChromosomeDatabase.get(chromosome, gen);
        chromosomeDBInfo.m = m;
        ChromosomeDatabase.addDBInfo(chromosomeDBInfo);
        if (MContained(output)) {
            float g = ChromosomeDatabase.G(chromosome, gen);
            chromosomeDBInfo = ChromosomeDatabase.get(chromosome, gen);
            chromosomeDBInfo.g = g;
            ChromosomeDatabase.addDBInfo(chromosomeDBInfo);
        }
        chromosomeDBInfo = ChromosomeDatabase.get(chromosome, gen);
        if (Float.isNaN((chromosomeDBInfo.ltl + chromosomeDBInfo.m + chromosomeDBInfo.g)
                / (Config.GWeight + Config.LTLWeight + Config.MWeight)))
            return 0;
        else
            return (chromosomeDBInfo.ltl + chromosomeDBInfo.m + chromosomeDBInfo.g)
                    / (Config.GWeight + Config.LTLWeight + Config.MWeight);
    }

    private static float LTL(InterpretorResults[] output, Chromosome chromosome, int gen) {
        float result = 0;
        ChromosomeDBInfo chromosomeDBInfo = ChromosomeDatabase.get(chromosome, gen);

        float tSafety = 0;
        float tLivelyness = 0;
        float tSegFault = 0;
        float tException = 0;
        float tExecutionTime = 0;
        float tIllegalOutput = 0;
        float tExpectedOutput = 0;

        for (InterpretorResults interpretorResults : output) {
            float res = 0;
            float safety = Safety(interpretorResults);
            res += safety;
            float livelyness = Livelyness(interpretorResults);
            res += livelyness;
            float segFault = SegFault(interpretorResults);
            res += livelyness;
            float exceptions = Exception(interpretorResults);
            res += exceptions;
            float executionTime = ExecutionTime(interpretorResults);
            res += executionTime;
            float illegalOutput = IllegalOutput(interpretorResults);
            res += illegalOutput;
            float expectedOutput = ExpectedOutput(interpretorResults);
            res += expectedOutput;
            result += res / FitnessConfig.weightsOfActiveProperties;
            tSafety += safety;
            tLivelyness += livelyness;
            tSegFault += segFault;
            tException += exceptions;
            tExecutionTime += executionTime;
            tIllegalOutput += illegalOutput;
            tExpectedOutput += expectedOutput;

        }
        chromosomeDBInfo.Safety = tSafety;
        chromosomeDBInfo.Livelyness = tLivelyness;
        chromosomeDBInfo.SegFault = tSegFault;
        chromosomeDBInfo.Exceptions = tException;
        chromosomeDBInfo.ExecutionTime = tExecutionTime;
        chromosomeDBInfo.IllegalOutput = tIllegalOutput;
        chromosomeDBInfo.ExpectedOutput = tExpectedOutput;
        ChromosomeDatabase.addDBInfo(chromosomeDBInfo);
        return (((float) Config.LTLWeight * result) / output.length);
    }

    private static float M(InterpretorResults[] output) {
        float result = 0;

        for (InterpretorResults interpretorResults : output) {
            if (Safety(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (Livelyness(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (SegFault(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (Exception(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (ExecutionTime(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (IllegalOutput(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (ExpectedOutput(interpretorResults) > 0) {
                result++;
                continue;
            }
        }
        return (result / output.length) * Config.MWeight;
    }

    private static boolean MContained(InterpretorResults[] output) {
        float result = 0;

        for (InterpretorResults interpretorResults : output) {
            if (Safety(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (Livelyness(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (SegFault(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (Exception(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (ExecutionTime(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (IllegalOutput(interpretorResults) > 0) {
                result++;
                continue;
            }
            if (ExpectedOutput(interpretorResults) > 0) {
                result++;
                continue;
            }
        }
        return result > 0;
    }

    private static float Safety(InterpretorResults output) {
        if (!FitnessConfig.Safety.enabled) {
            return 0;
        }
        float result = 0;
        if (!(output.errOut.equals("") && output.errOut.isEmpty())) {
            result += 1;
        } else if (output.stdOut.toUpperCase().contains("EXCEPTION")) {
            result += 1;
        }

        return ((float) result * FitnessConfig.Safety.weight);
    }

    private static float Livelyness(InterpretorResults output) {
        if (!FitnessConfig.Livelyness.enabled)
            return 0;

        float result = 0;
        if (output.studentExitCode != 0) {
            result += 1;
        }

        result = (float) (FitnessConfig.Livelyness.weight * result);
        return result;
    }

    private static float SegFault(InterpretorResults output) {
        if (FitnessConfig.Safety.enabled)
            return 0;

        if (!FitnessConfig.SegFault.enabled)
            return 0;

        float result = 0;
        if (output.stdOut.toLowerCase().contains("segfault")
                || output.stdOut.toLowerCase().contains("segmentation fault")
                || output.exeTime == 139) {
            result += 1;
        }
        result = (float) (FitnessConfig.SegFault.weight * result);
        return result;
    }

    private static float Exception(InterpretorResults output) {
        if (FitnessConfig.Safety.enabled)
            return 0;

        if (!FitnessConfig.SegFault.enabled)
            return 0;

        float result = 0;

        if (output.stdOut.toLowerCase().contains("exception")
                || output.stdOut.toLowerCase().contains("exceptions")) {
            result += 1;
        } else if (output.stdOut.toLowerCase().contains("exception")
                || output.stdOut.toLowerCase().contains("exceptions")) {
            result += 1;
        }

        result = (float) (FitnessConfig.Exceptions.weight * result);
        return result;
    }

    private static float ExecutionTime(InterpretorResults output) {
        if (!FitnessConfig.ExecutionTime.enabled)
            return 0;

        float result = 0;

        if (output.exeTime > FitnessConfig.ExecutionTime.maxTime) {
            result += 1;
        }
        result = (float) FitnessConfig.ExecutionTime.weight * result;
        return result;
    }

    private static float IllegalOutput(InterpretorResults output) {
        if (!FitnessConfig.IllegalOutput.enabled)
            return 0;

        if (FitnessConfig.IllegalOutput.words.length <= 0)
            return 0;

        float result = 0;

        for (String word : FitnessConfig.IllegalOutput.words) {
            if (output.stdOut.contains(word)) {
                result += 1;
            }
        }
        result = (float) (FitnessConfig.IllegalOutput.weight * result
                / (FitnessConfig.IllegalOutput.words.length));
        return result;
    }

    private static float ExpectedOutput(InterpretorResults output) {
        if (!FitnessConfig.ExpectedOutput.enabled)
            return 0;

        float result = FitnessConfig.ExpectedOutput.constantExpected(output);
        // float result = FitnessConfig.ExpectedOutput.

        result = (float) (FitnessConfig.ExpectedOutput.weight * result);
        return result;
    }
}
