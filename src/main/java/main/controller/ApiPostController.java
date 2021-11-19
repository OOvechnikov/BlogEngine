package main.controller;

import main.api.request.CommentRequest;
import main.api.request.ModerationRequest;
import main.api.request.PostRequest;
import main.api.request.VoteRequest;
import main.api.response.*;
import main.api.response.post.PostResponse;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api")
public class ApiPostController {

    private final PostService postService;

    @Autowired
    public ApiPostController(PostService postService) {
        this.postService = postService;
    }



    @GetMapping("/post")
    public PostResponse getPosts(@RequestParam(name = "offset", required = false) Integer offset,
                              @RequestParam(name = "limit", required = false) Integer limit,
                              @RequestParam(name = "mode", required = false) String mode) {
        return postService.getPostResponse(offset, limit, mode);
    }

    @PostMapping("/post")
    @PreAuthorize("hasAuthority('user:write')")
    public SimpleResultResponse postPost(@RequestBody PostRequest request,
                                           Principal principal) {
        ResultResponseWithErrors response = postService.savePost(request, principal);
        if (!response.getErrors().isEmpty()) {
            return response;
        }
        return new SimpleResultResponse(true);
    }

    @GetMapping("/post/{id}")
    public ResponseEntity<PostByIdResponse> getPostById(@PathVariable(name = "id") Integer id,
                                                        Principal principal) {
        PostByIdResponse response = postService.getPostById(id, principal);
        if (response == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/post/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public SimpleResultResponse updatePost(@RequestBody PostRequest request,
                                              @PathVariable("id") Integer id,
                                              Principal principal) {
        ResultResponseWithErrors response = postService.updatePost(request, id, principal);
        if (!response.getErrors().isEmpty()) {
            return response;
        }
        return new SimpleResultResponse(true);
    }

    @GetMapping("/post/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostResponse> getMyPosts(@RequestParam(name = "offset", required = false) Integer offset,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "status", required = false) String status,
                                   Principal principal) {
        return ResponseEntity.ok(postService.getMyPosts(offset, limit, status, principal));
    }

    @GetMapping("/post/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public PostResponse getModerationPosts(@RequestParam(name = "offset", required = false) Integer offset,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "status", required = false) String status,
                                   Principal principal) {
        return postService.getModeratedPosts(offset, limit, status, principal);
    }

    @GetMapping("/post/search")
    public PostResponse getPostsBySearch(@RequestParam(name = "offset", required = false) Integer offset,
                                         @RequestParam(name = "limit", required = false) Integer limit,
                                         @RequestParam(name = "query", required = false) String query) {
        return postService.getPostsBySearch(offset, limit, query);
    }

    @GetMapping("/calendar")
    public CalendarResponse getCalendar(@RequestParam(name = "year", required = false) Integer year) {
        return postService.getCalendarByYear(year);
    }

    @GetMapping("/post/byDate")
    public PostResponse getPostsByDate(@RequestParam(name = "offset", required = false) Integer offset,
                                       @RequestParam(name = "limit", required = false) Integer limit,
                                       @RequestParam(name = "date") String date) {
        return postService.getPostsByDate(offset, limit, date);
    }

    @GetMapping("/post/byTag")
    public PostResponse getPostsByTag(@RequestParam(name = "offset", required = false) Integer offset,
                                      @RequestParam(name = "limit", required = false) Integer limit,
                                      @RequestParam(name = "tag") String tag) {
        return postService.getPostsByTag(offset, limit, tag);
    }

    @PostMapping("/image")
    public ResponseEntity<Object> setImage(@RequestBody MultipartFile image) throws IOException {
        Object response = postService.saveImage(image);
        if (response instanceof ResultResponseWithErrors) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/comment")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Object> setComment(@RequestBody CommentRequest request,
                                                              Principal principal) {
        Object response = postService.saveComment(request, principal);
        if (response instanceof ResultAndErrorsResponse) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public SimpleResultResponse moderatePost(@RequestBody ModerationRequest request,
                                                Principal principal) {
        return postService.moderatePost(request, principal);
    }

    @PostMapping("/post/like")
    @PreAuthorize("hasAuthority('user:write')")
    public SimpleResultResponse postLike(@RequestBody VoteRequest voteRequest,
                                         Principal principal) {
        return postService.postVote(voteRequest, principal, 1);
    }

    @PostMapping("/post/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public SimpleResultResponse postDislike(@RequestBody VoteRequest voteRequest,
                                            Principal principal) {
        return postService.postVote(voteRequest, principal, -1);
    }

}
