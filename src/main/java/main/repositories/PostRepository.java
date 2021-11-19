package main.repositories;

import main.model.ModerationStatus;
import main.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThan(int isActive, ModerationStatus moderationStatus, Date time);
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeAsc(int isActive, ModerationStatus moderationStatus, Date time);
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeDesc(int isActive, ModerationStatus moderationStatus, Date time);
    @Query(value = "from Post where isActive = ?1 and moderationStatus = ?2 and time <= ?3 order by comments.size desc")
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByCommentsCount(int isActive, ModerationStatus moderationStatus, Date time);
    @Query(value = "SELECT *, (SELECT count(*) FROM post_votes pv JOIN posts p ON p.id = pv.post_id WHERE posts.id = pv.post_id AND value = 1) AS likes_count FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= curdate() " +
            "ORDER BY likes_count DESC",
            nativeQuery = true)
    List<Post> findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByLikesCount();
    @Query(value = "from Post where isActive = 1 and moderationStatus = 'ACCEPTED' and time <= current_time and (title LIKE concat('%', ?1, '%') or text LIKE concat('%', ?1, '%'))")
    List<Post> findAllByIsActiveAndAcceptedAndTimeLessThanNowAndTitleAndTextContaining(String query);
    @Query(value = "FROM Post WHERE isActive = 1 and moderationStatus = 'ACCEPTED' and time LIKE concat(?1, '%')")
    List<Post> findAllByIsActiveAndAcceptedAndByDate(String date);
    @Query(value = "SELECT p FROM Post p " +
            "JOIN Tag2Post tp ON p.id = tp.postId " +
            "JOIN Tag t ON tp.tagId = t.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND t.name = ?1")
    List<Post> findAllByIsActiveAndAcceptedAndTagEquals(String tag);

    List<Post> findAllByIsActiveAndUser_emailOrderByTimeDesc(int isActive, String email);
    List<Post> findAllByIsActiveAndModerationStatusAndUser_emailOrderByTimeDesc(int isActive, ModerationStatus moderationStatus, String email);

    @Query(value = "SELECT count(p) FROM Post p WHERE p.moderator = null AND p.isActive = 1")
    Integer qtyOfPostsNeededModeration();
    List<Post> findAllByIsActiveAndModeratorIsNullOrderByTimeDesc(int isActive);

}
