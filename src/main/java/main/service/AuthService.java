package main.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import main.api.request.*;
import main.api.response.*;
import main.model.CaptchaCode;
import main.model.User;
import main.repositories.CaptchaRepository;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.Date;

@Service
public class AuthService {

    @Value("${captcha.termOf}")
    private String captchaTermOf;

    private final CaptchaRepository captchaRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AuthenticationManager authenticationManager;
    private final ValidationService validationService;
    private final ResourceStorage resourceStorage;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public AuthService(CaptchaRepository captchaRepository, UserRepository userRepository, PostRepository postRepository, AuthenticationManager authenticationManager, ValidationService validationService, ResourceStorage resourceStorage, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.captchaRepository = captchaRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.authenticationManager = authenticationManager;
        this.validationService = validationService;
        this.resourceStorage = resourceStorage;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }


    public CaptchaResponse getCaptchaResponse() {
        Cage cage = new GCage();
        String code = cage.getTokenGenerator().next();
        String secret = RandomStringUtils.randomAlphabetic(20);
        String image = Base64.getEncoder().encodeToString(cage.draw(code));

        captchaRepository.save(new CaptchaCode(new Date(), code, secret));
        return new CaptchaResponse(secret, "data:image/png;base64, " + image);
    }

    @Transactional
    public ResultResponseWithErrors getRegisterResponse(RegisterRequest request) {
        captchaRepository.deleteByTimeBefore(new Date(new Date().getTime() - (long) Integer.parseInt(captchaTermOf) * 1000 * 60));
        ResultResponseWithErrors response = new ResultResponseWithErrors();

        validationService.validateCaptcha(request.getCaptchaSecret(), request.getCaptcha(), response);
        validationService.validateEMail(request.getEMail(), response);
        validationService.validatePassword(request.getPassword(), response);
        validationService.validateName(request.getName(), response);

        if (response.getErrors().isEmpty()) {
            userRepository.save(new User(
                    0,
                    new Date(),
                    request.getName(),
                    request.getEMail(),
                    passwordEncoder.encode(request.getPassword()))
            );
        }
        return response;
    }

    public SimpleResultResponse getLoginResponse(LoginRequest loginRequest) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            return new SimpleResultResponse();
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) auth.getPrincipal();
        return createLoginResponse(user.getUsername());
    }

    public LoginResponse getCheckResponse(Principal principal) {
        return createLoginResponse(principal.getName());
    }

    public ResultResponseWithErrors getMyProfileResponse(ProfileRequest request, Principal principal) throws IOException {
        ResultResponseWithErrors response = new ResultResponseWithErrors();
        User currentUser = userRepository.findByEmail(principal.getName());
        if (request.getName() != null && !request.getName().equals(currentUser.getName())) {
            validationService.validateName(request.getName(), response);
            currentUser.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(currentUser.getEmail())) {
            validationService.validateEMail(request.getEmail(), response);
            currentUser.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            validationService.validatePassword(request.getPassword(), response);
            if (passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
                response.getErrors().put("password", "Введен существующий пароль");
            } else {
                currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }
        if (request instanceof ProfileRequestWithPhoto) {
            validationService.validateImage(((ProfileRequestWithPhoto) request).getPhoto(), response);
            currentUser.setPhoto(resourceStorage.saveNewUserImage(((ProfileRequestWithPhoto) request).getPhoto(), currentUser));
        } else if (request instanceof  ProfileRequestWithoutPhoto && request.getRemovePhoto() == 1) {
            currentUser.setPhoto("");
        }
        if (response.getErrors().isEmpty()) {
            userRepository.save(currentUser);
        }

        return response;
    }

    public SimpleResultResponse getRestoreResponse(RestoreRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
        if (user == null) {
            return new SimpleResultResponse();
        }

        String code = RandomStringUtils.randomAlphanumeric(45);
        emailService.sendEmail(user.getEmail(), code);

        user.setCode(code);
        userRepository.save(user);

        return new SimpleResultResponse(true);
    }

    @Transactional
    public ResultResponseWithErrors getPostPasswordResponse(PasswordRequest request) {
        captchaRepository.deleteByTimeBefore(new Date(new Date().getTime() - (long) Integer.parseInt(captchaTermOf) * 1000 * 60));
        ResultResponseWithErrors response = new ResultResponseWithErrors();
        validationService.validateCaptcha(request.getCaptchaSecret(), request.getCaptcha(), response);
        validationService.validatePassword(request.getPassword(), response);
        User user = userRepository.findByCodeEquals(request.getCode());
        validationService.validateUserForPasswordChange(user, response);

        if (response.getErrors().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }
        return response;
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
                currentUser.getIsModerator() == 1 ? postRepository.qtyOfPostsNeededModeration() : 0,
                currentUser.getIsModerator() == 1);
        return new LoginResponse(userLoginResponse);
    }
}
