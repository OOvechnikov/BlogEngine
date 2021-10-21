package main.repositories;

import main.model.ModerationStatus;
import main.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query(value = "SELECT count(*) FROM posts WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= curdate()", nativeQuery = true)
    Integer findAllByIsActiveAndModerationStatusAcceptedAndTimeLessThanNow();

    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThan (int isActive, ModerationStatus moderationStatus, Date time);
    //List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThan (int isActive, ModerationStatus moderationStatus, Date time, Pageable pageable);
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeAsc (int isActive, ModerationStatus moderationStatus, Date time); //, Pageable pageable);
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeDesc (int isActive, ModerationStatus moderationStatus, Date time); //, Pageable pageable);
    @Query(value = "from Post where isActive = ?1 and moderationStatus = ?2 and time <= ?3 order by comments.size desc")
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByCommentsCount (int isActive, ModerationStatus moderationStatus, Date time); //, Pageable pageable);
    @Query(value = "SELECT *, (SELECT count(*) FROM post_votes pv JOIN posts p ON p.id = pv.post_id WHERE posts.id = pv.post_id AND value = 1) AS likes_count FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= curdate() ORDER BY likes_count DESC", nativeQuery = true)
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByLikesCount (int isActive, ModerationStatus moderationStatus, Date time); //, Pageable pageable);

}
