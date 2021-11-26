package main.service;

import main.api.response.ResultResponseWithErrors;
import main.model.CaptchaCode;
import main.model.User;
import main.repositories.CaptchaRepository;
import main.repositories.PostRepository;
import main.repositories.SettingsRepository;
import main.repositories.UserRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Service
public class ValidationService {

    private final CaptchaRepository captchaRepository;
    private final UserRepository userRepository;
    private final SettingsService settingsService;
    private final PostRepository postRepository;
    private final SettingsRepository settingsRepository;


    @Autowired
    public ValidationService(CaptchaRepository captchaRepository, UserRepository userRepository, SettingsService settingsService, PostRepository postRepository, SettingsRepository settingsRepository) {
        this.captchaRepository = captchaRepository;
        this.userRepository = userRepository;
        this.settingsService = settingsService;
        this.postRepository = postRepository;
        this.settingsRepository = settingsRepository;
    }


    public void validateCaptcha(String captchaSecret, String captcha, ResultResponseWithErrors response) {
        CaptchaCode captchaCode = captchaRepository.findBySecretCode(captchaSecret);
        if (captchaCode == null) {
            response.getErrors().put("code", "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">Запросить ссылку снова</a>");
        } else if (!captchaCode.getCode().equals(captcha)) {
            response.getErrors().put("captcha", "Код с картинки введен неверно");
        }
    }

    public void validateEMail(String email, ResultResponseWithErrors response) {
        if (userRepository.findByEmail(email) != null) {
            response.getErrors().put("email", "Этот e-mail уже зарегистрирован");
        } else if (!email.matches(".+@.+\\..+")) {
            response.getErrors().put("email", "Введен некорректный e-mail");
        } else if (email.isBlank()) {
            response.getErrors().put("email", "Email не может быть пустым");
        }
    }

    public void validatePassword(String password, ResultResponseWithErrors response) {
        if (password.length() < 6) {
            response.getErrors().put("password", "Пароль короче 6-ти символов");
        } else if (password.isBlank()) {
            response.getErrors().put("password", "Пароль не может быть пустым");
        }
    }

    public void validateName(String name, ResultResponseWithErrors response) {
        if (!name.matches("[A-Z][a-z]+ [A-Z][a-z]+")) {
            response.getErrors().put("name", "Имя указано неверно. Должны быть имя и фамилия через пробел латинскими буквами");
        } else if (!name.matches(".{6,40}")) {
            response.getErrors().put("name", "Имя указано неверно. Должно быть от 6 до 40 символов");
        } else if (name.isBlank()) {
            response.getErrors().put("name", "Имя не может быть пустым");
        }
    }

    public void validateTitle(String title, ResultResponseWithErrors response) {
        if (title == null || title.isBlank()) {
            response.getErrors().put("title", "Заголовок не установлен");
        } else if (title.length() < 3) {
            response.getErrors().put("title", "Заголовок короче 3 символов");
        }
    }

    public void validateText(String text, ResultResponseWithErrors response) {
        if (text == null || text.isBlank()) {
            response.getErrors().put("text", "Текст не установлен");
        } else if (text.length() < 50) {
            response.getErrors().put("text", "Текст короче 50 символов");
        }
    }

    public void validateImage(MultipartFile image, ResultResponseWithErrors response) {
        String originalFilename = image.getOriginalFilename();
        if (image.getSize() > 5 * 1024 * 1024) {
            response.getErrors().put("image", "Размер превышает 5 Mb");
        } else if (image.isEmpty()) {
            response.getErrors().put("image", "Файл пуст");
        } else if (originalFilename != null &&
                !FilenameUtils.getExtension(originalFilename).equalsIgnoreCase("jpg") &&
                !FilenameUtils.getExtension(originalFilename).equalsIgnoreCase("png")) {
            response.getErrors().put("image", "Допустимые типы: jpg, png");
        }
    }

    public void validateUserForPasswordChange(User user, ResultResponseWithErrors response) {
        if (user == null) {
            response.getErrors().put("user", "Неверный код восстановления");
        }
    }

    public void validatePostById(int id, ResultResponseWithErrors response) {
        if (postRepository.findById(id).isEmpty()) {
            response.getErrors().put("post", "Поста с таким id не существует");
        }
    }


    public boolean validateUserForStatistics(Principal principal) {
        if (settingsService.isStatisticsIsPublicOn()) {
            return true;
        } else if (principal == null) {
            return false;
        } else {
            return userRepository.findByEmail(principal.getName()).getIsModerator() == 1;
        }
    }

    public boolean validateRegistrationPossibility() {
        return settingsRepository.findMultiuserMode().equals("YES");
    }

    public boolean validatePostPremoderation() {
        return settingsRepository.findPostPremoderation().equals("YES");
    }

}
