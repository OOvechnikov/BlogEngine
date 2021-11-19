package main.service;

import main.api.response.SettingsRequestAndResponse;
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


    public SettingsRequestAndResponse getGlobalSettings() {
        SettingsRequestAndResponse settingsRequestAndResponse = new SettingsRequestAndResponse();
        List<GlobalSetting> globalSettingList = settingsRepository.findAll();
        for (GlobalSetting globalSetting : globalSettingList) {
            switch (globalSetting.getCode()) {
                case "MULTIUSER_MODE" : {
                     if (globalSetting.getValue().equals("YES"))
                         settingsRequestAndResponse.setMultiuserMode(true);
                     break;
                }
                case "POST_PREMODERATION" : {
                    if (globalSetting.getValue().equals("YES"))
                        settingsRequestAndResponse.setPostPremoderation(true);
                    break;
                }
                case "STATISTICS_IS_PUBLIC" : {
                    if (globalSetting.getValue().equals("YES"))
                        settingsRequestAndResponse.setStatisticIsPublic(true);
                    break;
                }
            }
        }
        return settingsRequestAndResponse;
    }

    public void putGlobalSettings(SettingsRequestAndResponse request) {
        List<GlobalSetting> globalSettingList = settingsRepository.findAll();
        for (GlobalSetting globalSetting : globalSettingList) {
            switch (globalSetting.getCode()) {
                case "MULTIUSER_MODE" : {
                    globalSetting.setValue(request.isMultiuserMode() ? "YES" : "NO");
                    break;
                }
                case "POST_PREMODERATION" : {
                    globalSetting.setValue(request.isPostPremoderation() ? "YES" : "NO");
                    break;
                }
                case "STATISTICS_IS_PUBLIC" : {
                    globalSetting.setValue(request.isStatisticIsPublic() ? "YES" : "NO");
                    break;
                }
            }
        }
        settingsRepository.saveAll(globalSettingList);
    }

    public boolean isMultiuserModeOn() {
        return settingsRepository.findMultiuserMode().equals("YES");
    }

    public boolean isStatisticsIsPublicOn() {
        return settingsRepository.findStatisticsIsPublic().equals("YES");
    }
}
