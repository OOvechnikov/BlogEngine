package main.api.response.post;

public class User {

    private int id;
    private String name;
    private String photo;


    public User(int id, String name, String photo) {
        this.id = id;
        this.name = name;
        this.photo = photo;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
