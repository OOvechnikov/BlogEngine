package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import main.api.request.LoginRequest;
import main.api.request.RegisterRequest;
import main.api.response.*;
import main.model.CaptchaCode;
import main.model.User;
import main.repositories.CaptchaRepository;
import main.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Base64;
import java.util.Date;

@Service
public class AuthService {

    private final CaptchaRepository captchaRepository;
    private final UserRepository userRepository;
    @Value("${captcha.termOf}")
    private String captchaTermOf;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(CaptchaRepository captchaRepository, UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.captchaRepository = captchaRepository;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
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

        if (response.isResult()) {
            userRepository.save(new User(0, new Date(), request.getName(), request.getEMail(), request.getPassword()));
        }
        return response;
    }

    public LoginResponse getLoginResponse(LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        return createLoginResponse(user.getUsername());
    }

    public LogoutResponse getLogoutResponse() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return new LogoutResponse();
    }

    public LoginResponse getCheckResponse(Principal principal) {
        return createLoginResponse(principal.getName());
    }



    private void validateCaptcha(RegisterRequest request, RegisterResponse response) {
        if (!captchaRepository.findBySecretCode(request.getCaptchaSecret()).isPresent()) {
            response.setResult(false);
            response.getErrors().put("captcha", "Нет такой капчи");
        } else
            if (!request.getCaptcha().equals(captchaRepository.findBySecretCode(request.getCaptchaSecret()).get().getCode())) {
            response.setResult(false);
            response.getErrors().put("captcha", "Код с картинки введен неверно");
        }
    }

    private void validateEMail(RegisterRequest request, RegisterResponse response) {
        if (userRepository.findByEmail(request.getEMail()) != null) {
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

    private LoginResponse createLoginResponse(String email) {
        main.model.User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new UsernameNotFoundException("user " + email + " not found");
        }
        UserLoginResponse userLoginResponse = new UserLoginResponse(
                currentUser.getId(),
                currentUser.getName(),
                currentUser.getPhoto(),
                currentUser.getEmail(),
                currentUser.getIsModerator() == 1,
                currentUser.getIsModerator() == 1 ? currentUser.getModeratedPostsWithStatusNEW().size() : 0,
                currentUser.getIsModerator() == 1);
        return new LoginResponse(true, userLoginResponse);
    }

}
