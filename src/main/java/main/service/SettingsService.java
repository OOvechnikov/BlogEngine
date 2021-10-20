package main.service;

import main.api.response.SettingsResponse;
import main.model.GlobalSetting;
import main.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettingsService {

    private final SettingsRepository settingsRepository;

    @Autowired
    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }



    public SettingsResponse getGlobalSettings() {
        SettingsResponse settingsResponse = new SettingsResponse();
        List<GlobalSetting> globalSettingList = settingsRepository.findAll();
        for (GlobalSetting globalSetting : globalSettingList) {
            switch (globalSetting.getCode()) {
                case "MULTIUSER_MODE" : {
                     if (globalSetting.getValue().equals("YES"))
                         settingsResponse.setMultiuserMode(true);
                     break;
                }
                case "POST_PREMODERATION" : {
                    if (globalSetting.getValue().equals("YES"))
                        settingsResponse.setPostPremoderation(true);
                    break;
                }
                case "STATISTICS_IS_PUBLIC" : {
                    if (globalSetting.getValue().equals("YES"))
                        settingsResponse.setStatisticIsPublic(true);
                    break;
                }
            }
        }
        return settingsResponse;
    }

}
