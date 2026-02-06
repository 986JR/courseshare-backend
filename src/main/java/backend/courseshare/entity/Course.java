package backend.courseshare.entity;

import backend.courseshare.entity.Users;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "courses",
        indexes = {
                @Index(name = "idx_courses_course_code", columnList = "course_code"),
                @Index(name = "idx_courses_title", columnList = "title")
        })
@Data
@NoArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Users createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Column(name = "course_code", unique = true, length = 100)
    private String courseCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Course(String title, String description, Users createdBy, String courseCode, Category category) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.courseCode = courseCode;
        this.category = category;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }


}
