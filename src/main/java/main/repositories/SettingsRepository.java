package main.repositories;

import main.model.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SettingsRepository extends JpaRepository<GlobalSetting, Integer> {

    @Query("SELECT gs.value FROM GlobalSetting gs WHERE gs.code = 'MULTIUSER_MODE'")
    String findMultiuserMode();
    @Query("SELECT gs.value FROM GlobalSetting gs WHERE gs.code = 'STATISTICS_IS_PUBLIC'")
    String findStatisticsIsPublic();
    @Query("SELECT gs.value FROM GlobalSetting gs WHERE gs.code = 'POST_PREMODERATION'")
    String findPostPremoderation();
}
