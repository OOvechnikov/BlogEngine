package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class RegisterResponse {
    private boolean result = true;
    private Map<String, String> errors = new HashMap<>();

    public RegisterResponse() {
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
