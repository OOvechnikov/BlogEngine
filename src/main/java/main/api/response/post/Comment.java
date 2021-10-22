package main.api.response.post;

public class Comment {
    private int id;
    private int timestamp;
    private String text;
    private User user;

    public Comment(int id, int timestamp, String text, User user) {
        this.id = id;
        this.timestamp = timestamp;
        this.text = text;
        this.user = user;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
