package main.repositories;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {

    long deleteByTimeBefore(Date time);
    CaptchaCode findBySecretCode(String secret);

}
