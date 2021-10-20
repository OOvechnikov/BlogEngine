package main.repositories;

import main.model.ModerationStatus;
import main.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThan (int isActive, ModerationStatus moderationStatus, Date time);

}
