package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class ResultAndErrorsResponse {

    private int id = 0;
    private boolean result = true;
    private String message;
    private Map<String, String> errors = new HashMap<>();


    public ResultAndErrorsResponse() {
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
