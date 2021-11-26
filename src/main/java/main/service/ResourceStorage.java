package main.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import main.model.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ResourceStorage {

    private final Cloudinary cloudinary;


    @Autowired
    public ResourceStorage(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }


    public String saveNewPostImage(MultipartFile image) throws IOException {
        String finalPath = RandomStringUtils.randomAlphabetic(2)
                + "/" + RandomStringUtils.randomAlphabetic(2)
                + "/" + RandomStringUtils.randomAlphabetic(2)
                + "/" + image.getOriginalFilename();
        Map params = ObjectUtils.asMap("public_id", "Blog/pictures/" + finalPath);
        Map uploadResult = cloudinary.uploader().upload(image.getBytes(), params);
        return uploadResult.get("url").toString();
    }

    public String saveNewUserImage(MultipartFile image, User user) throws IOException {
        Map params = ObjectUtils.asMap(
                "public_id", "Blog/users/" + user.getEmail(),
                "transformation", new Transformation<>().width(36).height(36)
        );
        Map uploadResult = cloudinary.uploader().upload(image.getBytes(), params);
        return uploadResult.get("url").toString();
    }
}
