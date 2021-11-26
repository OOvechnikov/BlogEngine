package main.api.request;

import org.springframework.web.multipart.MultipartFile;

public class ProfileRequestWithPhoto extends ProfileRequest {

    private MultipartFile photo;


    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }
}
