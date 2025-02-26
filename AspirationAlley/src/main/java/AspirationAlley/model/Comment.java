package AspirationAlley.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Base64;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String username;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "profile_picture", columnDefinition = "MEDIUMBLOB")
    private byte[] profilePicture;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }
    public String getCommentProfilePicture() {
        return profilePicture != null ? Base64.getEncoder().encodeToString(profilePicture) : null;
    }


    public String getFormattedTimestamp() {
        if (createdAt == null) {
            return "Unknown";
        }

        // Calculate the time difference between createdAt and current time
        long seconds = java.time.Duration.between(createdAt, java.time.LocalDateTime.now()).getSeconds();
        long minutes = java.util.concurrent.TimeUnit.SECONDS.toMinutes(seconds);
        long hours = java.util.concurrent.TimeUnit.SECONDS.toHours(seconds);
        long days = java.util.concurrent.TimeUnit.SECONDS.toDays(seconds);

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "") + " ago";
        }
    }
}
