package main.service;

import main.api.responce.PostResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PostService {

    public PostResponse getPostResponse() {
        PostResponse postResponse = new PostResponse();
        postResponse.setCount(0);
        postResponse.setPosts(new ArrayList<>());
        return postResponse;
    }

}
