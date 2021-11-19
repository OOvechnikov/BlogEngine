package main.controller;

import main.api.request.LoginRequest;
import main.api.request.PasswordRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestoreRequest;
import main.api.response.*;
import main.service.AuthService;
import main.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final AuthService authService;
    private final ValidationService validationService;


    @Autowired
    public ApiAuthController(AuthService authService, ValidationService validationService) {
        this.authService = authService;
        this.validationService = validationService;
    }


    @PostMapping("/login")
    public ResponseEntity<SimpleResultResponse> login(@RequestBody LoginRequest loginRequest) {
        SimpleResultResponse response = authService.getLoginResponse(loginRequest);
        if (response.isResult()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(new SimpleResultResponse());
    }

    @GetMapping("/logout")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<SimpleResultResponse> logout() {
        return ResponseEntity.ok(new SimpleResultResponse(true));
    }

    @GetMapping("/check")
    public ResponseEntity<SimpleResultResponse> check(Principal principal) {
        if (principal == null) {
            return ResponseEntity.ok(new SimpleResultResponse());
        }
        return ResponseEntity.ok(authService.getCheckResponse(principal));
    }

    @GetMapping("/captcha")
    public CaptchaResponse getCaptcha() {
        return authService.getCaptchaResponse();
    }

    @PostMapping("/register")
    public ResponseEntity<SimpleResultResponse> getRegistration(@RequestBody RegisterRequest request) {
        if (!validationService.validateRegistrationPossibility()) {
            return ResponseEntity.notFound().build();
        }
        ResultResponseWithErrors response = authService.getRegisterResponse(request);
        if (response.getErrors().isEmpty()) {
            return ResponseEntity.ok(new SimpleResultResponse(true));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/restore")
    public SimpleResultResponse postRestore(@RequestBody RestoreRequest request) {
        return authService.getRestoreResponse(request);
    }

    @PostMapping("/password")
    public SimpleResultResponse postPassword(@RequestBody PasswordRequest request) {
        ResultResponseWithErrors response = authService.getPostPasswordResponse(request);
        if (response.getErrors().isEmpty()) {
            return new SimpleResultResponse(true);
        }
        return response;
    }
}
