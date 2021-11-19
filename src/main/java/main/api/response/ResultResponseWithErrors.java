package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class ResultResponseWithErrors extends SimpleResultResponse {

    private final Map<String, String> errors = new HashMap<>();


    public Map<String, String> getErrors() {
        return errors;
    }
}
