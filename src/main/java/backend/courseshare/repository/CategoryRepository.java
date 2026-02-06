package backend.courseshare.repository;

import backend.courseshare.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByNameIgnoreCase(String name);

}
