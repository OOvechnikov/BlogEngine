package main.service;

import main.api.responce.TagResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TagService {

    public TagResponse getTag() {
        TagResponse tagResponse = new TagResponse();
        tagResponse.setTags(new ArrayList<>());
        return tagResponse;
    }

}
