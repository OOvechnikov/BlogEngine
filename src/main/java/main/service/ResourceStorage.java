package main.service;

import main.model.User;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ResourceStorage {

    @Value("${path.upload.pictures}")
    private String postImageUploadPath;
    @Value("${path.upload.users}")
    private String userImageUploadPath;


    public String saveNewPostImage(MultipartFile image) throws IOException {
        if (!new File(postImageUploadPath).exists()) {
            Files.createDirectories(Paths.get(postImageUploadPath));
        }
        String finalPath = postImageUploadPath + "/" + RandomStringUtils.randomAlphabetic(2)
                + "/" + RandomStringUtils.randomAlphabetic(2)
                + "/" + RandomStringUtils.randomAlphabetic(2);
        if (!new File(finalPath).exists()) {
            Files.createDirectories(Paths.get(finalPath));
        }
        Path path = Paths.get(finalPath, image.getOriginalFilename());

        image.transferTo(path);
        return path.toString();
    }

    public String saveNewUserImage(MultipartFile image, User user) throws IOException {
        if (!new File(userImageUploadPath).exists()) {
            Files.createDirectories(Paths.get(userImageUploadPath));
        }
        Path path = Paths.get(userImageUploadPath, user.getEmail() + '.' + FilenameUtils.getExtension(image.getOriginalFilename()));

        image.transferTo(path);
        return path.toString();
    }
}
