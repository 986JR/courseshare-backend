package backend.courseshare.repository;

import backend.courseshare.entity.CategoryAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryAliasRepository extends JpaRepository<CategoryAlias, Long> {
    Optional<CategoryAlias> findByAliasIgnoreCase(String alias);
    List<CategoryAlias> findByCategory_Slug(String categorySlug);
    boolean existsByAliasIgnoreCase(String alias);
}
