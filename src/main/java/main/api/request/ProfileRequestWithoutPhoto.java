package main.api.request;

public class ProfileRequestWithoutPhoto extends ProfileRequest {

    private String photo;


    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
