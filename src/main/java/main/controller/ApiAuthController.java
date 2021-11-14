package main.controller;

import main.api.request.LoginRequest;
import main.api.request.RegisterRequest;
import main.api.response.CaptchaResponse;
import main.api.response.LoginResponse;
import main.api.response.LogoutResponse;
import main.api.response.RegisterResponse;
import main.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthService authService;

    @Autowired
    public ApiAuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.getLoginResponse(loginRequest));
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<LogoutResponse> logout() {
        return ResponseEntity.ok(authService.getLogoutResponse());
    }

    @GetMapping("/check")
    public ResponseEntity<LoginResponse> check(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(new LoginResponse(false, null));
        }
        return ResponseEntity.ok(authService.getCheckResponse(principal));
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
