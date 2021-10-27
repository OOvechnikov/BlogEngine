package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import main.api.response.CaptchaResponse;
import main.api.response.RegisterResponse;
import main.model.CaptchaCode;
import main.model.User;
import main.repositories.CaptchaRepository;
import main.repositories.UserRepository;
import main.api.request.RegisterRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Date;

@Service
public class AuthService {

    private final CaptchaRepository captchaRepository;
    private final UserRepository userRepository;
    @Value("${captcha.termOf}")
    private String captchaTermOf;

    @Autowired
    public AuthService(CaptchaRepository captchaRepository, UserRepository userRepository) {
        this.captchaRepository = captchaRepository;
        this.userRepository = userRepository;
    }



    @Transactional
    public CaptchaResponse getCaptchaResponse() {
        Cage cage = new GCage();
        String code = cage.getTokenGenerator().next();
        String secret = RandomStringUtils.randomAlphabetic(20);
        String image = Base64.getEncoder().encodeToString(cage.draw(code));

        captchaRepository.deleteByTimeBefore(new Date(new Date().getTime() - (long) Integer.parseInt(captchaTermOf) * 60 * 1000));
        captchaRepository.save(new CaptchaCode(new Date(), code, secret));
        return new CaptchaResponse(secret, "data:image/png;base64, " + image);
    }

    public RegisterResponse getRegisterResponse(RegisterRequest request) {
        RegisterResponse response = new RegisterResponse();

        validateCaptcha(request, response);
        validateEMail(request, response);
        validatePassword(request, response);
        validateName(request, response);

        if (!response.isResult()) {
            return response;
        }

        userRepository.save(new User(0, new Date(), request.getName(), request.getEMail(), request.getPassword()));
        return response;
    }



    private void validateCaptcha(RegisterRequest request, RegisterResponse response) {
        CaptchaCode captchaCode = captchaRepository.findBySecretCode(request.getCaptchaSecret());
        if (captchaCode == null) {
            response.setResult(false);
        }
        if (!request.getCaptcha().equals(captchaCode.getCode())) {
            response.setResult(false);
            response.getErrors().put("captcha", "Код с картинки введен неверно");
        }
    }

    private void validateEMail(RegisterRequest request, RegisterResponse response) {
        User user = userRepository.findByEmail(request.getEMail());
        if (user != null) {
            response.setResult(false);
            response.getErrors().put("email", "Этот e-mail уже зарегистрирован");
        } else if (!request.getEMail().matches(".+@.+\\..+")) {
            response.setResult(false);
            response.getErrors().put("email", "Введен некорректный e-mail");
        }
    }

    private void validatePassword(RegisterRequest request, RegisterResponse response) {
        if (request.getPassword().length() < 6) {
            response.setResult(false);
            response.getErrors().put("password", "Пароль короче 6-ти символов");
        }
    }

    private void validateName(RegisterRequest request, RegisterResponse response) {
        if (!request.getName().matches("[A-Z][a-z]+ [A-Z][a-z]+")) {
            response.setResult(false);
            response.getErrors().put("name", "Имя указано неверно. Должны быть имя и фамилия через пробел латинскими буквами");
        } else if (!request.getName().matches(".{6,40}")) {
            response.setResult(false);
            response.getErrors().put("name", "Имя указано неверно. Должно быть от 6 до 40 символов");
        }
    }

}
