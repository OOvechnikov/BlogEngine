package main.service;

import main.api.response.tag.TagResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TagService {

    private final JdbcTemplate jdbcTemplate;

    private final Logger logger = Logger.getLogger(TagService.class);

    @Autowired
    public TagService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public TagResponse getTagResponseJDBC(String query) {
        long time = new Date().getTime();
        if (query == null) query = "";
        String customQuery = "SELECT t.name AS tag_name, count(t.id) AS tag_freq, " +
                "(select count(*) from posts p where p.moderation_status = 'ACCEPTED' AND p.is_active = 1 AND p.time <= curdate()) AS posts_qty " +
                "FROM tags t JOIN tag2post t2p ON t.id = t2p.tag_id JOIN posts p ON t2p.post_id = p.id " +
                "WHERE p.moderation_status = 'ACCEPTED' AND p.is_active = 1 AND p.time <= now() AND t.name LIKE '" + query + "%'" +
                "GROUP BY t.id " +
                "ORDER BY tag_freq DESC";

        List<CustomResponse> customResponseList = jdbcTemplate.query(customQuery, (ResultSet rs, int rowNum) -> {
            CustomResponse result = new CustomResponse();
            result.setTagName(rs.getString("tag_name"));
            result.setTagFreq(rs.getInt("tag_freq"));
            result.setPostsQty(rs.getInt("posts_qty"));
            return result;
        });

        double k = 0;
        List<main.api.response.tag.Tag> responseTags = new ArrayList<>();
        for (int i = 0; i < customResponseList.size(); i++) {
            CustomResponse currQuery = customResponseList.get(i);
            currQuery.setDWeight((double) currQuery.getTagFreq() / currQuery.postsQty);
            if (i == 0) {
                k = 1 / currQuery.getDWeight();
            }
            currQuery.setWeight(currQuery.getDWeight() * k);
            responseTags.add(new main.api.response.tag.Tag(currQuery.getTagName(), currQuery.getWeight()));
        }
        logger.info("Work time with JDBC2 method: " + (new Date().getTime() - time) + "ms");
        return new TagResponse(responseTags);
    }


    static class CustomResponse {
        private String tagName;
        private int tagFreq;
        private int postsQty;
        private double dWeight;
        private double weight;


        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public int getTagFreq() {
            return tagFreq;
        }

        public void setTagFreq(int tagFreq) {
            this.tagFreq = tagFreq;
        }

        public void setPostsQty(int postsQty) {
            this.postsQty = postsQty;
        }

        public double getDWeight() {
            return dWeight;
        }

        public void setDWeight(double dWeight) {
            this.dWeight = dWeight;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
