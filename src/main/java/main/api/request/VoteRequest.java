package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VoteRequest {

    @JsonProperty("post_id")
    private int postId;


    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }
}
