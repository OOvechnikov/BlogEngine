package main.controller;

import main.api.request.ProfileRequest;
import main.api.response.*;
import main.api.response.tag.TagResponse;
import main.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagService tagService;
    private final AuthService authService;
    private final StatisticsService statisticsService;
    private final ValidationService validationService;


    @Autowired
    public ApiGeneralController(InitResponse initResponse, SettingsService settingsService, TagService tagService, AuthService authService, StatisticsService statisticsService, ValidationService validationService) {
        this.initResponse = initResponse;
        this.settingsService = settingsService;
        this.tagService = tagService;
        this.authService = authService;
        this.statisticsService = statisticsService;
        this.validationService = validationService;
    }


    @GetMapping("/init")
    public InitResponse getInit() {
        return initResponse;
    }

    @GetMapping("/settings")
    public SettingsRequestAndResponse getSettings() {
        return settingsService.getGlobalSettings();
    }

    @PutMapping(value = "/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public void putSettings(@RequestBody SettingsRequestAndResponse request) {
        settingsService.putGlobalSettings(request);
    }

    @GetMapping("/tag")
    public TagResponse getTag(@RequestParam(name = "query", required = false) String query) {
        return tagService.getTagResponseJDBC(query);
    }

    @PostMapping("/profile/my")
    public SimpleResultResponse postMyyProfile(@ModelAttribute ProfileRequest request,
                                           Principal principal) throws IOException {
        ResultResponseWithErrors response = authService.getMyProfileResponse(request, principal);
        if (response.getErrors().isEmpty()) {
            return new SimpleResultResponse(true);
        }
        return response;
    }

    @GetMapping("/statistics/my")
    @PreAuthorize("hasAuthority('user:write')")
    public StatisticsResponse getMyStatistics(Principal principal) {
        return statisticsService.getStatistics(principal);
    }

    @GetMapping("/statistics/all")
    public ResponseEntity<StatisticsResponse> getAllStatistics(Principal principal) {
        if (validationService.validateUserForStatistics(principal)) {
            return ResponseEntity.ok(statisticsService.getAllStatistics());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
