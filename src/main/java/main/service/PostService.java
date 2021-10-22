package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostByIdResponse;
import main.api.response.post.PostResponse;
import main.api.response.post.User;
import main.model.ModerationStatus;
import main.model.Post;
import main.model.PostVote;
import main.repositories.PostRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

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
        return getPostResponse(posts.size(), getPostsFromDBByParameters(offset, limit, mode), offset, limit);
    }

    public PostResponse getPostsBySearch(Integer offset, Integer limit, String query) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (query == null || query.equals("") || query.matches(" +")) return getPostResponseByPage(offset, limit, "recent");

        long time = new Date().getTime();
        List<Post> posts = postRepository.findAllByIsActiveAndAcceptedAndTimeLessThanNowAndTitleAndTextContaining(query);

        logger.info("Working time with query request '" + query + "': " + (new Date().getTime() - time) + "ms");
        return getPostResponse(posts.size(), posts, offset, limit);
    }

    public CalendarResponse getCalendarByYear(Integer year) {
        if (year == null || year == 0) year = GregorianCalendar.getInstance().get(Calendar.YEAR);

        List<Post> posts = getActiveAcceptedLessThenNowPosts();

        //DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
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

        CalendarResponse calendarResponse = new CalendarResponse();
        calendarResponse.setYears(responseYears);
        calendarResponse.setPosts(responsePosts);
        return calendarResponse;
    }

    public PostResponse getPostsByDate(Integer offset, Integer limit, String date) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;

        long time = new Date().getTime();
        List<Post> posts =  postRepository.findAllByIsActiveAndAcceptedAndByDate(date);

        logger.info("Working time with date request '" + date + "': " + (new Date().getTime() - time) + "ms");
        return getPostResponse(posts.size(), posts, offset, limit);
    }

    public PostResponse getPostsByTag(Integer offset, Integer limit, String tag) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;

        long time = new Date().getTime();
        List<Post> posts = postRepository.findAllByIsActiveAndAcceptedAndTagEquals(tag);

        logger.info("Working time with tag request '" + tag + "': " + (new Date().getTime() - time) + "ms");
        return getPostResponse(posts.size(), posts, offset, limit);
    }

    public PostByIdResponse getPostById(Integer id) {

        return null;
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

    private PostResponse getPostResponse(int count, List<Post> posts, int offset, int limit) {
        PostResponse postResponse = new PostResponse();
        postResponse.setCount(count);
        postResponse.setPosts(getPostsForPostResponse(getFormattedList(posts, offset, limit)));
        return postResponse;
    }

}
