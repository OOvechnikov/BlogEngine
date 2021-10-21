package main.service;

import main.api.response.post.*;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.repositories.PostRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final Logger logger = Logger.getLogger(PostService.class);

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }



    public PostResponse getPostResponseByPage(Integer offset, Integer limit, String mode) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (mode == null) mode = "recent";

        long time = new Date().getTime();
        PostResponse postResponse = new PostResponse();
        List<Post> posts = getActiveAcceptedLessThenNowPosts();
        postResponse.setCount(posts.size());
        postResponse.setPosts(getPostsForPostResponse(getPostsFromDBByParameters(offset, limit, mode)));
        logger.info("Working time with '" + mode + "' parameter: " + (new Date().getTime() - time) + "ms");
        return postResponse;
    }

    public PostResponse getPostsBySearch(Integer offset, Integer limit, String query) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (query == null || query.equals("") || query.matches(" +")) return getPostResponseByPage(offset, limit, "recent");

        long time = new Date().getTime();
        PostResponse postResponse = new PostResponse();
        List<Post> posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanAndTitleAndTextContaining(query);
        postResponse.setCount(posts.size());
        postResponse.setPosts(getPostsForPostResponse(getFormattedList(posts, offset, limit)));
        logger.info("Working time with query request '" + query + "': " + (new Date().getTime() - time) + "ms");
        return postResponse;
    }



    private List<Post> getActiveAcceptedLessThenNowPosts() {
        return postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThan(1, ModerationStatus.ACCEPTED, new Date());
    }

    private List<Post> getPostsFromDBByParameters(Integer offset, Integer limit, String mode) {
        List<Post> posts;
        switch (mode) {
            case "recent" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeDesc(1, ModerationStatus.ACCEPTED, new Date());
                return getFormattedList(posts, offset, limit);
            }
            case "popular" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByCommentsCount(1, ModerationStatus.ACCEPTED, new Date());
                return getFormattedList(posts, offset, limit);
            }
            case "best" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByLikesCount();
                return getFormattedList(posts, offset, limit);
            }
            case "early" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeAsc(1, ModerationStatus.ACCEPTED, new Date());
                return getFormattedList(posts, offset, limit);
            }
        }
        return null;
    }

    private List<main.api.response.post.Post> getPostsForPostResponse(List<Post> posts) {
        List<main.api.response.post.Post> showedPosts = new ArrayList<>();
        for (Post post : posts) {
            int likeCount = 0;
            int dislikeCount = 0;
            for (PostVote vote : post.getVotes()) {
                if (vote.getValue() == 1) likeCount++;
                else dislikeCount++;
            }
            showedPosts.add(new main.api.response.post.Post(post.getId(),
                    post.getTime().getTime() / 1000,
                    new User(post.getUser().getId(), post.getUser().getName()),
                    post.getTitle(),
                    getAnnounce(post.getText()),
                    likeCount,
                    dislikeCount,
                    post.getComments().size(),
                    post.getViewCount()
            ));
        }
        return showedPosts;
    }

    private String getAnnounce(String text) {
        text = HtmlUtils.htmlEscape(text);
        if (text.length() <= 150) return text;
        else {
            text = text.substring(0, 150) + "...";
        }
        return text;
    }

    private List<Post> getFormattedList(List<Post> posts, Integer offset, Integer limit) {
        if (offset >= posts.size()) return new ArrayList<>();
        if (offset + limit > posts.size()) return posts.subList(offset, posts.size());
        return posts.subList(offset, offset + limit);
    }

    private List<Post> getPostsBySearchQuery (List<Post> posts, String query) {
        return posts.stream()
                .filter(post -> post.getTitle().contains(query) || post.getText().contains(query))
                .sorted(Comparator.comparing(Post::getTime).reversed())
                .collect(Collectors.toList());
    }

}
