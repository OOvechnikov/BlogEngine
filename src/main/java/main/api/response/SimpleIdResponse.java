package main.api.response;

public class SimpleIdResponse {

    private int id;


    public SimpleIdResponse(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
