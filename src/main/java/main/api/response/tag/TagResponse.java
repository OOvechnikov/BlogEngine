package main.api.response.tag;

import java.util.List;

public class TagResponse {

    private List<Tag> tags;

    public TagResponse(List<Tag> tags) {
        this.tags = tags;
    }



    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
