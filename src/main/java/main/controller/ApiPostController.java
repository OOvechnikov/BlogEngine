package main.controller;

import main.api.response.CalendarResponse;
import main.api.response.PostByIdResponse;
import main.api.response.post.Post;
import main.api.response.post.PostResponse;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/post/{id}")
    public ResponseEntity<PostByIdResponse> getPostById(@PathVariable(name = "id") Integer id,
                                                        Principal principal) {
        PostByIdResponse response = postService.getPostById(id, principal);
        if (response == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/my")
    @PreAuthorize("hasAuthority('user:write')")
    public PostResponse getMyPosts(@RequestParam(name = "offset", required = false) Integer offset,
                                   @RequestParam(name = "limit", required = false) Integer limit,
                                   @RequestParam(name = "status", required = false) String status,
                                   Principal principal) {
        return postService.getMyPosts(offset, limit, status, principal);
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

}
