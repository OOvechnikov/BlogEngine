package main.service;

import main.api.response.CalendarResponse;
import main.api.response.PostByIdResponse;
import main.api.response.post.Comment;
import main.api.response.post.PostResponse;
import main.api.response.post.User;
import main.model.*;
import main.repositories.PostRepository;
import main.repositories.UserRepository;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Logger logger = Logger.getLogger(PostService.class);

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }


    public PostResponse getPostResponse(Integer offset, Integer limit, String mode) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (mode == null) mode = "recent";

        long time = new Date().getTime();
        List<Post> posts = getPostsFromDBByParameters(mode, null);

        logger.info("Working time with '" + mode + "' parameter: " + (new Date().getTime() - time) + "ms");
        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }

    public PostResponse getPostsBySearch(Integer offset, Integer limit, String query) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (query == null || query.equals("") || query.matches(" +")) return getPostResponse(offset, limit, "recent");

        long time = new Date().getTime();
        List<Post> posts = postRepository.findAllByIsActiveAndAcceptedAndTimeLessThanNowAndTitleAndTextContaining(query);

        logger.info("Working time with query request '" + query + "': " + (new Date().getTime() - time) + "ms");
        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }

    public CalendarResponse getCalendarByYear(Integer year) {
        if (year == null || year == 0) year = GregorianCalendar.getInstance().get(Calendar.YEAR);

        Integer finalYear = year;
        Set<Integer> responseYears = getActiveAcceptedLessThenNowPosts().stream().map(p -> p.getTime().getYear()).map(y -> y + 1900).collect(Collectors.toSet());
        Map<String, Long> responsePosts = getActiveAcceptedLessThenNowPosts().stream()
                .filter(p -> p.getTime().getYear() + 1900 == finalYear)
                .collect(Collectors.groupingBy(p -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(p.getTime());
                    return dtf.format(LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
                },
                        Collectors.counting()));
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

    public PostByIdResponse getPostById(Integer id, Principal principal) {
        Post post;
        if (!postRepository.findById(id).isPresent()) {
            return null;
        }
        post = postRepository.findById(id).get();

        main.model.User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByEmail(principal.getName());
        }
        if ((currentUser == null && (post.getIsActive() == 0 || post.getModerationStatus() != ModerationStatus.ACCEPTED || post.getTime().after(new Date())))
                ||
                (currentUser != null && !currentUser.equals(post.getUser()) && !currentUser.equals(post.getModerator()))) {
            return null;
        }

        //likes/dislikes
        int likeCount = (int) post.getVotes().stream().filter(v -> v.getValue() == 1).count();
        int dislikeCount = post.getVotes().size() - likeCount;

        //comments
        List<Comment> comments = post.getComments().stream()
                .map(c -> new Comment(
                        c.getId(),
                        (int) (c.getTime().getTime() / 1000),
                        c.getText(),
                        new User(
                                c.getUser().getId(),
                                c.getUser().getName(),
                                c.getUser().getPhoto())))
                .collect(Collectors.toList());

        //tags
        List<String> tags = post.getTags().stream().map(Tag::getName).collect(Collectors.toList());

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

    public PostResponse getMyPosts(Integer offset, Integer limit, String status, Principal principal) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (status == null) status = "inactive";

        List<Post> posts = getPostsFromDBByParameters(status, principal);

        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }

    public PostResponse getModeratedPosts(Integer offset, Integer limit, String status, Principal principal) {
        if (offset == null) offset = 0;
        if (limit == null) limit = 10;
        if (status == null) status = "new";
        if (status.equals("declined")) {
            status = "moderated_declined";
        }

        List<Post> posts = getPostsFromDBByParameters(status, principal);

        return new PostResponse(posts.size(), getPostsForPostResponse(getFormattedList(posts, offset, limit)));
    }



    private List<Post> getActiveAcceptedLessThenNowPosts() {
        return postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThan(1, ModerationStatus.ACCEPTED, new Date());
    }

    private List<Post> getPostsFromDBByParameters(String mode, Principal principal) {
        List<Post> posts;
        switch (mode) {
            case "recent" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeDesc(1, ModerationStatus.ACCEPTED, new Date());
                return posts;
            }
            case "popular" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByCommentsCount(1, ModerationStatus.ACCEPTED, new Date());
                return posts;
            }
            case "best" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByLikesCount();
                return posts;
            }
            case "early" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeAsc(1, ModerationStatus.ACCEPTED, new Date());
                return posts;
            }


            case "inactive" : {
                posts = postRepository.findAllByIsActiveAndUser_email(0, principal.getName());
                return posts;
            }
            case "pending" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndUser_email(1, ModerationStatus.NEW, principal.getName());
                return posts;
            }
            case "declined" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndUser_email(1, ModerationStatus.DECLINED, principal.getName());
                return posts;
            }
            case "published" : {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndUser_email(1, ModerationStatus.ACCEPTED, principal.getName());
                return posts;
            }


            case "new" : {
                main.model.User currentUser = userRepository.findByEmail(principal.getName());
                posts = currentUser.getModeratedPosts().stream()
                        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus().equals(ModerationStatus.NEW))
                        .collect(Collectors.toList());
                return posts;
            }
            case "moderated_declined" : {
                main.model.User currentUser = userRepository.findByEmail(principal.getName());
                posts = currentUser.getModeratedPosts().stream()
                        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus().equals(ModerationStatus.DECLINED))
                        .collect(Collectors.toList());
                return posts;
            }
            case "accepted" : {
                main.model.User currentUser = userRepository.findByEmail(principal.getName());
                posts = currentUser.getModeratedPosts().stream()
                        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus().equals(ModerationStatus.ACCEPTED))
                        .collect(Collectors.toList());
                return posts;
            }

            default: return new ArrayList<>();
        }
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
