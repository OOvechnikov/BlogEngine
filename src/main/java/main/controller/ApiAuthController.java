package main.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiAuthController {


//    @RequestMapping("/auth/check")
//    public AuthResponse authResponse() {
//        return authResponse;
//    }

    @RequestMapping("/auth/check")
    public String authResponse() {
        return "{\"result\": false}";
    }

}
