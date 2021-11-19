package main.repositories;

import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByEmail(String eMail);
    User findByCodeEquals(String code);

}
