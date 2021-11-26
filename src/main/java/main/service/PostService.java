package main.service;

import main.api.request.CommentRequest;
import main.api.request.ModerationRequest;
import main.api.request.PostRequest;
import main.api.request.VoteRequest;
import main.api.response.*;
import main.api.response.post.Comment;
import main.api.response.post.PostResponse;
import main.api.response.post.User;
import main.model.*;
import main.repositories.*;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostCommentRepository postCommentRepository;
    private final ResourceStorage resourceStorage;
    private final ValidationService validationService;
    private final PostVoteRepository postVoteRepository;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Logger logger = Logger.getLogger(PostService.class);


    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository, TagRepository tagRepository, PostCommentRepository postCommentRepository, ResourceStorage resourceStorage, ValidationService validationService, PostVoteRepository postVoteRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.postCommentRepository = postCommentRepository;
        this.resourceStorage = resourceStorage;
        this.validationService = validationService;
        this.postVoteRepository = postVoteRepository;
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
        if (query == null || query.isBlank()) {
            return getPostResponse(offset, limit, "recent");
        }

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
        List<Post> posts = postRepository.findAllByIsActiveAndAcceptedAndByDate(date);

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
        if (postRepository.findById(id).isEmpty()) {
            return null;
        }
        post = postRepository.findById(id).get();

        main.model.User currentUser = null;
        if (principal != null) {
            currentUser = userRepository.findByEmail(principal.getName());
        }

        if ((post.getIsActive() == 1 && post.getModerationStatus().equals(ModerationStatus.ACCEPTED) && post.getTime().before(new Date())) ||
                (currentUser != null && currentUser.equals(post.getUser())) ||
                (currentUser != null && currentUser.getIsModerator() == 1 &&
                        (currentUser.getModeratedPosts().contains(post) || post.getModerationStatus().equals(ModerationStatus.NEW)))) {

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
                    .sorted(Comparator.comparingInt(Comment::getTimestamp).reversed())
                    .collect(Collectors.toList());
            //tags
            List<String> tags = post.getTags().stream().map(Tag::getName).collect(Collectors.toList());

            //viewCount increase
            if (currentUser == null || (!currentUser.equals(post.getUser()) && currentUser.getIsModerator() == 0)) {
                post.setViewCount(post.getViewCount() + 1);
                postRepository.save(post);
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
        } else {
            return null;
        }
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

    public ResultResponseWithErrors savePost(PostRequest request, Principal principal) {
        ResultResponseWithErrors response = new ResultResponseWithErrors();
        Post post = new Post();
        fillPostFields(post, request, response);
        if (!response.getErrors().isEmpty()) {
            return response;
        }

        if (!validationService.validatePostPremoderation()) {
            if (request.getActive() == 1) {
                post.setModerationStatus(ModerationStatus.ACCEPTED);
            } else {
                post.setModerationStatus(ModerationStatus.NEW);
            }
        } else {
            post.setModerationStatus(ModerationStatus.NEW);
        }

        post.setUser(userRepository.findByEmail(principal.getName()));
        postRepository.save(post);
        return response;
    }

    public ResultResponseWithErrors updatePost(PostRequest request, Integer id, Principal principal) {
        ResultResponseWithErrors response = new ResultResponseWithErrors();
        if (postRepository.findById(id).isEmpty()) {
            response.setResult(false);
            response.getErrors().put("post", "Поста с таким id не существует");
        }
        Post post = postRepository.findById(id).get();
        main.model.User currentUser = userRepository.findByEmail(principal.getName());

        if (currentUser.equals(post.getUser()) ||
                (currentUser.getIsModerator() == 1 && (currentUser.getModeratedPosts().contains(post) || post.getModerationStatus().equals(ModerationStatus.NEW)))) {
            fillPostFields(post, request, response);
            if (!response.getErrors().isEmpty()) {
                return response;
            }
            if (userRepository.findByEmail(principal.getName()).getIsModerator() == 0) {
                post.setModerationStatus(ModerationStatus.NEW);
                post.setModerator(null);
            }
            postRepository.save(post);
        } else {
            response.setResult(false);
            response.getErrors().put("auth", "Вы не имеете права редактировать этот пост");
        }
        return response;
    }

    public Object saveComment(CommentRequest request, Principal principal) {
        ResultResponseWithErrors response = new ResultResponseWithErrors();
        validationService.validatePostById(request.getPostId(), response);
        validationService.validateText(request.getText(), response);
        if (!response.getErrors().isEmpty()) {
            return response;
        }

        int commentId = postCommentRepository.findAll().size() + 1;
        SimpleIdResponse idResponse = new SimpleIdResponse(commentId);
        postCommentRepository.save(new PostComment(
                commentId,
                request.getParentId(),
                postRepository.findById(request.getPostId()).get(),
                userRepository.findByEmail(principal.getName()),
                new Date(),
                request.getText()
        ));
        return idResponse;
    }

    public SimpleResultResponse moderatePost(ModerationRequest request, Principal principal) {
        if ((!request.getDecision().equals("accept") && !request.getDecision().equals("decline"))
                || postRepository.findById(request.getPostId()).isEmpty()) {
            return new SimpleResultResponse();
        }
        Post post = postRepository.findById(request.getPostId()).get();
        post.setModerationStatus(request.getDecision().equals("accept") ? ModerationStatus.ACCEPTED : ModerationStatus.DECLINED);
        post.setModerator(userRepository.findByEmail(principal.getName()));
        postRepository.save(post);
        return new SimpleResultResponse(true);
    }

    public Object saveImage(MultipartFile image) throws IOException {
        ResultResponseWithErrors response = new ResultResponseWithErrors();
        validationService.validateImage(image, response);
        if (!response.getErrors().isEmpty()) {
            return response;
        }
        return resourceStorage.saveNewPostImage(image);

    }

    public SimpleResultResponse postVote(VoteRequest voteRequest, Principal principal, int vote) {
        main.model.User currentUser = userRepository.findByEmail(principal.getName());
        if (postRepository.findById(voteRequest.getPostId()).isEmpty()) {
            return new SimpleResultResponse();
        }
        Post post = postRepository.findById(voteRequest.getPostId()).get();

        PostVote pv = postVoteRepository.findByPost_IdAndUser_id(voteRequest.getPostId(), currentUser.getId());
        if (pv != null) {
            if (vote == pv.getValue()) {
                return new SimpleResultResponse();
            } else {
                pv.setValue(vote);
                postVoteRepository.save(pv);
                return new SimpleResultResponse(true);
            }
        }

        postVoteRepository.save(new PostVote(currentUser, post, new Date(), vote));
        return new SimpleResultResponse(true);
    }


    private List<Post> getActiveAcceptedLessThenNowPosts() {
        return postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThan(1, ModerationStatus.ACCEPTED, new Date());
    }

    private List<Post> getPostsFromDBByParameters(String mode, Principal principal) {
        List<Post> posts;
        switch (mode) {
            case "recent": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeDesc(1, ModerationStatus.ACCEPTED, new Date());
                return posts;
            }
            case "popular": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByCommentsCount(1, ModerationStatus.ACCEPTED, new Date());
                return posts;
            }
            case "best": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByLikesCount();
                return posts;
            }
            case "early": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndTimeLessThanOrderByTimeAsc(1, ModerationStatus.ACCEPTED, new Date());
                return posts;
            }


            case "inactive": {
                posts = postRepository.findAllByIsActiveAndUser_emailOrderByTimeDesc(0, principal.getName());
                return posts;
            }
            case "pending": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndUser_emailOrderByTimeDesc(1, ModerationStatus.NEW, principal.getName());
                return posts;
            }
            case "declined": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndUser_emailOrderByTimeDesc(1, ModerationStatus.DECLINED, principal.getName());
                return posts;
            }
            case "published": {
                posts = postRepository.findAllByIsActiveAndModerationStatusAndUser_emailOrderByTimeDesc(1, ModerationStatus.ACCEPTED, principal.getName());
                return posts;
            }


            case "new": {
                posts = postRepository.findAllByIsActiveAndModeratorIsNullOrderByTimeDesc(1);
                return posts;
            }
            case "moderated_declined": {
                main.model.User currentUser = userRepository.findByEmail(principal.getName());
                posts = currentUser.getModeratedPosts().stream()
                        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus().equals(ModerationStatus.DECLINED))
                        .sorted(Comparator.comparing(Post::getTime).reversed())
                        .collect(Collectors.toList());
                return posts;
            }
            case "accepted": {
                main.model.User currentUser = userRepository.findByEmail(principal.getName());
                posts = currentUser.getModeratedPosts().stream()
                        .filter(p -> p.getIsActive() == 1 && p.getModerationStatus().equals(ModerationStatus.ACCEPTED))
                        .sorted(Comparator.comparing(Post::getTime).reversed())
                        .collect(Collectors.toList());
                return posts;
            }

            default:
                return new ArrayList<>();
        }
    }

    private List<main.api.response.post.Post> getPostsForPostResponse(List<Post> posts) {
        return posts.stream()
                .map(p -> {
                    int likeCount = 0;
                    int dislikeCount = 0;
                    for (PostVote vote : p.getVotes()) {
                        int i = vote.getValue() == 1 ? likeCount++ : dislikeCount++;
                    }
                    return new main.api.response.post.Post(p.getId(),
                            (int) (p.getTime().getTime() / 1000),
                            new User(p.getUser().getId(), p.getUser().getName(), null),
                            p.getTitle(),
                            getAnnounce(p.getText()),
                            likeCount,
                            dislikeCount,
                            p.getComments().size(),
                            p.getViewCount()
                    );
                })
                .collect(Collectors.toList());
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

    private void fillPostFields(Post post, PostRequest request, ResultResponseWithErrors response) {
        validationService.validateTitle(request.getTitle(), response);
        validationService.validateText(request.getText(), response);
        if (!response.getErrors().isEmpty()) {
            return;
        }

        List<Tag> newTags = new ArrayList<>();
        List<Tag> tags = Stream.of(request.getTags())
                .map(rt -> {
                    Tag tag = tagRepository.findByNameEquals(rt);
                    if (tag != null){
                        return tag;
                    } else {
                        Tag newTag = new Tag(rt, new ArrayList<>());
                        newTags.add(newTag);
                        return newTag;
                    }
                })
                .collect(Collectors.toList());;
        tagRepository.saveAll(newTags);

        post.setIsActive(request.getActive());
        post.setTime(request.getTimestamp() < new Date().getTime() ? new Date() : new Date(request.getTimestamp()));
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setTags(tags);
    }
}
