package backend.courseshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_code_counters", uniqueConstraints = @UniqueConstraint(columnNames = "prefix"))
@Data
@NoArgsConstructor
public class CourseCodeCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 8, unique = true)
    private String prefix;


    @Column(name = "last_number", nullable = false)
    private int lastNumber;

    public CourseCodeCounter(String prefix, int lastNumber) {
        this.prefix = prefix;
        this.lastNumber = lastNumber;
    }
}
