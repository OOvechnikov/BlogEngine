package main.service;

import main.api.responce.SettingsResponse;
import main.model.GlobalSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SettingsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SettingsResponse getGlobalSettings() {



        SettingsResponse settingsResponse = new SettingsResponse();
        settingsResponse.setMultiuserMode(true);
        settingsResponse.setStatisticIsPublic(true);
        return settingsResponse;
    }

}
