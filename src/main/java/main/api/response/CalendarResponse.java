package main.api.response;

import java.util.Map;
import java.util.Set;

public class CalendarResponse {
    private Set<Integer> years;
    private Map<String, Long> posts;

    public CalendarResponse(Set<Integer> years, Map<String, Long> posts) {
        this.years = years;
        this.posts = posts;
    }



    public Set<Integer> getYears() {
        return years;
    }

    public void setYears(Set<Integer> years) {
        this.years = years;
    }

    public Map<String, Long> getPosts() {
        return posts;
    }

    public void setPosts(Map<String, Long> posts) {
        this.posts = posts;
    }
}
