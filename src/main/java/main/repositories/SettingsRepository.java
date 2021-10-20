package main.repositories;

import main.model.GlobalSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettingsRepository extends JpaRepository<GlobalSetting, Integer> {



}
