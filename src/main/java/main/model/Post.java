package main.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "is_active", nullable = false)
    private int isActive;

    @Column(name = "moderation_status", nullable = false)
    private Enum<ModerationStatus> moderationStatus;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "moderator_id")
    private User moderator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = false)
    private String title;

    @Column(length = 65 * 1024, nullable = false)
    private String text;

    @Column(nullable = false)
    private int viewCount;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    private List<PostVote> votes;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post")
    private List<PostComment> comments;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tag2post",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;



    public int getId() {
        return id;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public Enum<ModerationStatus> getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(Enum<ModerationStatus> moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public List<PostVote> getVotes() {
        return new ArrayList<>(votes);
    }

    public void setVotes(List<PostVote> votes) {
        this.votes = votes;
    }

    public List<PostComment> getComments() {
        return new ArrayList<>(comments);
    }

    public void setComments(List<PostComment> comments) {
        this.comments = comments;
    }

    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

}
