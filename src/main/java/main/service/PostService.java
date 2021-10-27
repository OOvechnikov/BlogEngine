package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostByIdResponse;
import main.api.response.post.Comment;
import main.api.response.post.PostResponse;
import main.api.response.post.User;
import main.model.*;
import main.repositories.PostRepository;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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
        List<Post> posts = getActiveAcceptedLessThenNowPosts();

        logger.info("Working time with '" + mode + "' parameter: " + (new Date().getTime() - time) + "ms");
        return new PostResponse(posts.size(), getPostsForPostResponse(getPostsFromDBByParameters(offset, limit, mode)));
    }

    public PostResponse getPostsBySearch(Integer offset, Integer limit, String query) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (query == null || query.equals("") || query.matches(" +")) return getPostResponseByPage(offset, limit, "recent");

        long time = new Date().getTime();
        List<Post> posts = postRepository.findAllByIsActiveAndAcceptedAndTimeLessThanNowAndTitleAndTextContaining(query);

        logger.info("Working time with query request '" + query + "': " + (new Date().getTime() - time) + "ms");
        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }

    public CalendarResponse getCalendarByYear(Integer year) {
        if (year == null || year == 0) year = GregorianCalendar.getInstance().get(Calendar.YEAR);

        List<Post> posts = getActiveAcceptedLessThenNowPosts();

        Set<Integer> responseYears = new TreeSet<>();
        Map<String, Integer> responsePosts = new HashMap();
        for (Post post : posts) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(post.getTime());
            responseYears.add(calendar.get(Calendar.YEAR));
            if (calendar.get(Calendar.YEAR) == year) {
                LocalDate ld = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
                String date = dtf.format(ld);
                if (!responsePosts.containsKey(date))
                    responsePosts.put(date, 1);
                else responsePosts.put(date, responsePosts.get(date) + 1);
            }
        }

        return new CalendarResponse(responseYears, responsePosts);
    }

    public PostResponse getPostsByDate(Integer offset, Integer limit, String date) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;

        long time = new Date().getTime();
        List<Post> posts =  postRepository.findAllByIsActiveAndAcceptedAndByDate(date);

        logger.info("Working time with date request '" + date + "': " + (new Date().getTime() - time) + "ms");
        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }

    public PostResponse getPostsByTag(Integer offset, Integer limit, String tag) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;

        long time = new Date().getTime();
        List<Post> posts = postRepository.findAllByIsActiveAndAcceptedAndTagEquals(tag);

        logger.info("Working time with tag request '" + tag + "': " + (new Date().getTime() - time) + "ms");
        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }

    public PostByIdResponse getPostById(Integer id) {
        Post post;
        if (!postRepository.findById(id).isPresent()) {
            return null;
        }

        post = postRepository.findById(id).get();
        if (post.getIsActive() == 0 || post.getModerationStatus() != ModerationStatus.ACCEPTED || post.getTime().after(new Date())) {
            return null;
        }

        //likes/dislikes
        int likeCount = 0;
        int dislikeCount = 0;
        for (PostVote vote : post.getVotes()) {
            if (vote.getValue() == 1) likeCount++;
            else dislikeCount++;
        }

        //comments
        List<PostComment> commentsToPost = postRepository.findCommentToPostById(id);
        List<Comment> comments = new ArrayList<>();
        for (PostComment postComment : commentsToPost) {
            comments.add(new Comment(
                            postComment.getId(),
                            (int) (postComment.getTime().getTime() / 1000),
                            postComment.getText(),
                            new User(
                                    postComment.getUser().getId(),
                                    postComment.getUser().getName(),
                                    postComment.getUser().getPhoto())
                    )
            );
        }

        //tags
        List<String> tags = new ArrayList<>();
        for (Tag tag : post.getTags()) {
            tags.add(tag.getName());
        }

        return new PostByIdResponse(
                post.getId(),
                (int) (post.getTime().getTime() / 1000),
                post.getIsActive() == 1,
                new User(post.getUser().getId(), post.getUser().getName(), null),
                post.getTitle(),
                post.getText(),
                likeCount,
                dislikeCount,
                post.getViewCount(),
                comments,
                tags
        );
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
                    (int) (post.getTime().getTime() / 1000),
                    new User(post.getUser().getId(), post.getUser().getName(), null),
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
        text = Jsoup.parse(text).text();
        if (text.length() <= 150) return text;
        else {
            text = text.substring(0, 146) + "...";
        }
        return text;
    }

    private List<Post> getFormattedList(List<Post> posts, Integer offset, Integer limit) {
        if (offset >= posts.size()) return new ArrayList<>();
        if (offset + limit > posts.size()) return posts.subList(offset, posts.size());
        return posts.subList(offset, offset + limit);
    }

}
