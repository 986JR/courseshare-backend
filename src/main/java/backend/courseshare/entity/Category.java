package backend.courseshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name"),
                @UniqueConstraint(columnNames = "slug")
        },
        indexes = {
                @Index(name = "idx_categories_name", columnList = "name"),
                @Index(name = "idx_categories_slug", columnList = "slug")
        })
@Data
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 200)
    private String name;


    @Column(nullable = false, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<CategoryAlias> aliases = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public Category(String name, String slug, String description) {
        this.name = name;
        this.slug = slug;
        this.description = description;
    }
}
