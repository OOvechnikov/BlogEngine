package main.api.response;

public class SimpleResultResponse {

    private boolean result;


    public SimpleResultResponse() {
    }

    public SimpleResultResponse(boolean result) {
        this.result = result;
    }


    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
