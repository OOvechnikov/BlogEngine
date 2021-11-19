package main.service;

import main.api.response.StatisticsResponse;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Date;
import java.util.List;

@Service
public class StatisticsService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Autowired
    public StatisticsService(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }


    public StatisticsResponse getStatistics(Principal principal) {
        return buildStatisticsResponse(userRepository.findByEmail(principal.getName()).getPublishedPosts());
    }

    public StatisticsResponse getAllStatistics() {
        return buildStatisticsResponse(postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeAsc(1, ModerationStatus.ACCEPTED, new Date()));
    }


    private StatisticsResponse buildStatisticsResponse(List<Post> posts) {
        int postsCount = posts.size();

        int likesCount = 0;
        int dislikesCount = 0;
        for (Post post : posts) {
            for (PostVote vote : post.getVotes()) {
                int i = vote.getValue() == 1 ? likesCount++ : dislikesCount++;
            }
        }

        int viewsCount = posts.stream()
                .map(Post::getViewCount)
                .mapToInt(v -> v)
                .sum();

        long firstPublication = posts.stream()
                .mapToLong(p -> p.getTime().getTime() / 1000)
                .min().orElse(0);

        return new StatisticsResponse(
                postsCount,
                likesCount,
                dislikesCount,
                viewsCount,
                firstPublication
        );
    }
}
