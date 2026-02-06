package backend.courseshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name= "enrollments",
       uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_user_course",
                         columnNames = {"user_id", "course_id"})
       },
        indexes = {
        @Index(name = "idx_enrollments_public_id", columnList = "public_id"),
                @Index(name = "idx_enrollments_user_id", columnList = "user_id"),
                @Index(name = "idx_errollments_user_id", columnList = "course_id")
        }
)
@Data
@NoArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private String publicId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @PrePersist
    protected void onCreate() {
        if(enrolledAt == null) enrolledAt =Instant.now();
    }

    public Enrollment(String publicId, Users user, Course course) {
        this.publicId = publicId;
        this.user = user;
        this.course = course;
    }
}
