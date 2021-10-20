package main.service;

import main.api.response.post.*;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.repositories.PostRepository;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }



    public PostResponse getPostResponse(int offset, int limit, String mode) {
        PostResponse postResponse = new PostResponse();

        List<Post> posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThan(1, ModerationStatus.NEW, new Date());
        postResponse.setCount(posts.size());

        if (limit == 0 ) limit = 10;

        List<main.api.response.post.Post> showedPosts = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            Post postFromDB = posts.get(i);
            int likeCount = 0;
            int dislikeCount = 0;
            for (PostVote vote : postFromDB.getVotes()) {
                if (vote.getValue() == 1) likeCount++;
                else dislikeCount++;
            }
            showedPosts.add(new main.api.response.post.Post(postFromDB.getId(),
                    postFromDB.getTime().getTime() / 1000,
                    new User(postFromDB.getUser().getId(), postFromDB.getUser().getName()),
                    postFromDB.getTitle(),
                    getAnnounce(postFromDB.getText()),
                    likeCount,
                    dislikeCount,
                    postFromDB.getComments().size(),
                    postFromDB.getViewCount()
                    ));
        }
        postResponse.setPosts(showedPosts);
        return postResponse;
    }

    private String getAnnounce(String text) {
        text = HtmlUtils.htmlEscape(text);
        if (text.length() <= 150) return text;
        else {
            text = text.substring(0, 150) + "...";
        }
        return text;
    }

}
