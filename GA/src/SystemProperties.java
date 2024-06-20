import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

abstract public class SystemProperties {
    final boolean enabled;
    final double weight;

    SystemProperties(JSONObject jsonObject) {
        enabled = (boolean) jsonObject.get("enabled");
        weight = (double) jsonObject.get("weight");
    }
}

class Safety extends SystemProperties {
    Safety(JSONObject jsonObject) {
        super(jsonObject);
    }
}

class Livelyness extends SystemProperties {
    Livelyness(JSONObject jsonObject) {
        super(jsonObject);
    }
}

class SegFault extends SystemProperties {
    SegFault(JSONObject jsonObject) {
        super(jsonObject);
    }
}

class Exceptions extends SystemProperties {
    Exceptions(JSONObject jsonObject) {
        super(jsonObject);
    }
}

class ExecutionTime extends SystemProperties {
    final Long maxTime;
    ExecutionTime(JSONObject jsonObject) {
        super(jsonObject);
        maxTime = (Long)jsonObject.get("maxTime");
    }
}

@SuppressWarnings("unchecked")
class IllegalOutput extends SystemProperties {
    final String[] illegalWords;
    IllegalOutput(JSONObject jsonObject) {
        super(jsonObject);
        illegalWords = (String[])((JSONArray)jsonObject.get("words")).toArray(new String[0]);
    }
}

class ExpectedOutput extends SystemProperties {
    final boolean exactMatch;
    ExpectedOutput(JSONObject jsonObject) {
        super(jsonObject);
        exactMatch = (boolean)jsonObject.get("exactMatch");
    }
}
