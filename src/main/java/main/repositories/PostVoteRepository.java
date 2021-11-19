package main.repositories;

import main.model.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {

    PostVote findByPost_IdAndUser_id(int postId, int userId);

}
