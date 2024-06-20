import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@SuppressWarnings("rawtypes")
class GeneConfig {
    public final GeneDataType geneDataType;
    private final Integer  maxValue;
    private final Integer  minValue;
    public final Integer[] invalidValues;
    public final Class dataType;

    public Integer  maxValue() {
        return maxValue;
    }

    public Integer  minValue() {
        return minValue;
    }

    public GeneConfig(JSONObject jsonObject) {
        geneDataType = GeneDataType.IntegerGDT;

        dataType = Integer.class;
        maxValue = ((Long)jsonObject.get("maxValue")).intValue();
        minValue = ((Long)jsonObject.get("minValue")).intValue();
        geneDataType.max = maxValue;
        geneDataType.min = minValue;

        Object[] resArr = null;
        try {
            JSONArray temp = ((JSONArray) jsonObject.get("invalidValues"));
            resArr = new Object[temp.size()];
            for (int i = 0; i < resArr.length; i++) {
                resArr[i] = temp.get(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            invalidValues = new Integer[resArr.length];
            for (int i = 0; i < resArr.length; i++) {
                invalidValues[i] = ((Long)resArr[i]).intValue();
            }
        }
    }

    static GeneConfig getGeneConfig(JSONObject jsonObject) {
        return new GeneConfig(jsonObject);
    }

    public Integer  convertFromBin(String str) {
        Integer  val = geneDataType.convertFromBin(str);
        return val;
    }

    public int numBits() {
        return geneDataType.numBits();
    }

    public Integer generateGene() {
        Integer val = geneDataType.randVal(maxValue, minValue);
        List<Integer> notAllowed = Arrays.asList(invalidValues);
        while (notAllowed.contains(val)) {
            val = geneDataType.randVal(maxValue, minValue);
        }
        return val;
    }

    public boolean validate(Integer val) {
        if (val < minValue || val > maxValue)
            return false;

        for (Integer inValid : invalidValues) {
            if (inValid == val)
                return false;
        }

        return true;
    }

    public String toBinaryString(Integer val) {
        return geneDataType.toBinaryString(val);
    }
}

enum GeneDataType {
    IntegerGDT {
        @Override
        public Integer convertFromBin(String str) {
            String sign = str.substring(0,1);
            String subStr = str.substring(1);
            Long l = Long.parseLong(subStr, 2);
            if(sign.charAt(0) == '1'){
                return -1*l.intValue();
            } 
            return l.intValue();
        }

        @Override
        public int numBits() {
            int minVal = (int)min;
            if((int)min < 0){
                minVal *= -1;
            }
            int maxVal = (int)max;
            if((int)maxVal < 0){
                maxVal *= -1;
            }
            int minLength = Integer.toBinaryString(minVal).length();
            int maxLength = Integer.toBinaryString(maxVal).length();
            if(minLength > maxLength){
                return minLength+1;
            } else {
                return maxLength+1;
            }
        }

        @Override
        public Integer randVal(Object max, Object min) {
            return Config.random.nextInt((Integer) max - (Integer) min) + (Integer) min;
        }

        @Override
        public String convertToBinary(Object val) {
            int iVal = (int)val;
            if(iVal < 0){
                iVal*= -1;
            }
            String temp = Integer.toBinaryString((Integer) iVal);
            String padded = pad(temp, numBits()-1);
            if((Integer)val < 0){
                padded = "1" + padded;
            } else {
                padded = "0" + padded;
            }
            return padded;
            // long l = java.lang.Integer.toUnsignedLong((java.lang.Integer)val);
            // return Long.toBinaryString(l);
        }

        @Override
        public Integer convert(Long val) throws IncorrectValueException {
            return val.intValue();
        }

        @Override
        public Integer convert(java.lang.Double val) throws IncorrectValueException {
            throw new IncorrectValueException(val, this);
        }

        @Override
        public Integer convert(String val) throws IncorrectValueException {
            throw new IncorrectValueException(val, this);
        }

        @Override
        public Integer convert(java.lang.Boolean val) throws IncorrectValueException {
            throw new IncorrectValueException(val, this);
        }

        @Override
        String toBinaryString(Integer val) {
            return convertToBinary(val);
        }
    };

    Object max;
    Object min;

    public abstract <T> Integer  convertFromBin(String str);

    public abstract int numBits();

    public abstract <T> Integer  randVal(Object max, Object min);

    public abstract String convertToBinary(Object val);

    public abstract <T> Integer  convert(Long val) throws IncorrectValueException;

    public abstract <T> Integer  convert(Double val) throws IncorrectValueException;

    public abstract <T> Integer  convert(String val) throws IncorrectValueException;

    public abstract <T> Integer  convert(Boolean val) throws IncorrectValueException;

    public <T> Integer  convert(Object val) throws IncorrectValueException {
        if (val instanceof Long)
            return convert((Long) val);
        if (val instanceof java.lang.Double)
            return convert((java.lang.Double) val);
        if (val instanceof String)
            return convert((String) val);
        if (val instanceof Boolean)
            return convert((Boolean) val);
        throw new IncorrectValueException(val, this);
    }

    class IncorrectValueException extends Exception {
        public IncorrectValueException(Long val, GeneDataType geneDataType) {
            super("Incorrect value: " + val + " for type: " + geneDataType.name());
        }

        public IncorrectValueException(Double val, GeneDataType geneDataType) {
            super("Incorrect value: " + val.toString() + " for type: " + geneDataType.name());
        }

        public IncorrectValueException(String val, GeneDataType geneDataType) {
            super("Incorrect value: " + val + " for type: " + geneDataType.name());
        }

        public IncorrectValueException(Boolean val, GeneDataType geneDataType) {
            super("Incorrect value: " + val + " for type: " + geneDataType.name());
        }

        public IncorrectValueException(Object val, GeneDataType geneDataType) {
            super("Incorrect value: " + val + " for type: " + geneDataType.name());
        }
    }

    private static String pad(String str, int numBits) {
        String temp = str;
        if(str.length() > numBits){
            return str.substring(str.length()-numBits);
        }
        while (temp.length() < numBits) {
            temp = "0" + temp;
        }
        return temp;
    }

    abstract String toBinaryString(Integer val);
}
