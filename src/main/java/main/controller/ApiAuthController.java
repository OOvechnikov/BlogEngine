package main.controller;

import main.api.response.CaptchaResponse;
import main.api.response.RegisterResponse;
import main.request.RegisterRequest;
import main.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthService authService;

    @Autowired
    public ApiAuthController(AuthService authService) {
        this.authService = authService;
    }



    @GetMapping("/check")
    public String authResponse() {
        return "{\"result\": false}";
    }

    @GetMapping("/captcha")
    public CaptchaResponse getCaptcha() {
        return authService.getCaptchaResponse();
    }

    @PostMapping("/register")
    public RegisterResponse getRegistration(@RequestBody RegisterRequest registerRequest) {
        return authService.getRegisterResponse(registerRequest);
    }

}
