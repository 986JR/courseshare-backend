package backend.courseshare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category_aliases",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "alias")
        },
        indexes = {
                @Index(name = "idx_category_alias_alias", columnList = "alias")
        })
@Data
@NoArgsConstructor
public class CategoryAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 150)
    private String alias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public CategoryAlias(String alias, Category category) {
        this.alias = alias;
        this.category = category;
    }
}
