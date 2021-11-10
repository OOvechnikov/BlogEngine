package main.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "is_moderator", nullable = false)
    private int isModerator;

    @Column(name = "reg_time", nullable = false)
    private Date regTime;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String code;

    @Column(length = 65 * 1024)
    private String photo;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "moderator")
    private List<Post> moderatedPosts;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Post> posts;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<PostVote> postVotes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<PostComment> comments;

    public User() {
    }

    public User(int isModerator, Date regTime, String name, String email, String password) {
        this.isModerator = isModerator;
        this.regTime = regTime;
        this.name = name;
        this.email = email;
        this.password = password;
    }



    public Role getRole() {
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }

    public List<Post> getModeratedPostsWithStatusNEW() {
        return moderatedPosts.stream()
                .filter(p -> p.getModerationStatus().equals(ModerationStatus.NEW) && p.getIsActive() == 1)
                .collect(Collectors.toList());
    }



    public int getId() {
        return id;
    }

    public int getIsModerator() {
        return isModerator;
    }

    public void setIsModerator(int isModerator) {
        this.isModerator = isModerator;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<Post> getModeratedPosts() {
        return new ArrayList<>(moderatedPosts);
    }

    public void setModeratedPosts(List<Post> moderatedPosts) {
        this.moderatedPosts = moderatedPosts;
    }

    public List<Post> getPosts() {
        return new ArrayList<>(posts);
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }

    public List<PostVote> getPostVotes() {
        return new ArrayList<>(postVotes);
    }

    public void setPostVotes(List<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    public List<PostComment> getComments() {
        return new ArrayList<>(comments);
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }
}
