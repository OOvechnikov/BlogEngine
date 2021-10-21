package main.controller;

import main.api.response.post.PostResponse;
import main.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiPostController {

    private PostService postService;

    @Autowired
    public ApiPostController(PostService postService) {
        this.postService = postService;
    }



    @GetMapping("/post")
    public PostResponse posts(@RequestParam(name = "offset", required = false) Integer offset,
                              @RequestParam(name = "limit", required = false) Integer limit,
                              @RequestParam(name = "mode", required = false) String mode) {
        return postService.getPostResponseByPage(offset, limit, mode);
    }

}
