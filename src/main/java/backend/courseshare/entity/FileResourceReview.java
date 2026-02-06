package backend.courseshare.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "file_resource_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_file_review",
                columnNames = {"user_id", "file_id"}
        ),
        indexes = {
                @Index(name = "idx_fr_reviews_file_id", columnList = "file_id"),
                @Index(name = "idx_fr_reviews_user_id", columnList = "user_id")
        }
)
public class FileResourceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = true)
    private Integer rating;

    @Column(columnDefinition = "TEXT", length = 4000)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private FileResource file;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // getters + setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public FileResource getFile() {
        return file;
    }

    public void setFile(FileResource file) {
        this.file = file;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
