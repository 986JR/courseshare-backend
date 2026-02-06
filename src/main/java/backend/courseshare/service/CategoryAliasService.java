package backend.courseshare.service;

import backend.courseshare.dto.alias.CategoryAliasResponse;
import backend.courseshare.dto.alias.CreateCategoryAliasRequest;
import backend.courseshare.entity.Category;
import backend.courseshare.entity.CategoryAlias;
import backend.courseshare.repository.CategoryAliasRepository;
import backend.courseshare.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryAliasService {

    private final CategoryAliasRepository aliasRepo;
    private final CategoryRepository categoryRepo;

    public CategoryAliasService(CategoryAliasRepository aliasRepo, CategoryRepository categoryRepo) {
        this.aliasRepo = aliasRepo;
        this.categoryRepo = categoryRepo;
    }

    /**
     * Create a new alias pointing to a category (lookup by slug).
     * Throws 404 if category not found, 409 if alias already exists.
     */
    @Transactional
    public CategoryAliasResponse createAlias(CreateCategoryAliasRequest req) {
        String alias = req.alias().trim();
        String categorySlug = req.categorySlug().trim();

        if (aliasRepo.existsByAliasIgnoreCase(alias)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alias already exists");
        }

        Category category = categoryRepo.findBySlug(categorySlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        CategoryAlias entity = new CategoryAlias();
        entity.setAlias(alias);
        entity.setCategory(category);

        CategoryAlias saved = aliasRepo.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryAliasResponse> listAll() {
        return aliasRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryAliasResponse> listByCategorySlug(String categorySlug) {
        return aliasRepo.findByCategory_Slug(categorySlug).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryAliasResponse findByIdOrThrow(Long id) {
        CategoryAlias a = aliasRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alias not found"));
        return toDto(a);
    }

    /**
     * Full update (PUT) — reuse Create DTO.
     */
    @Transactional
    public CategoryAliasResponse updateAlias(Long id, CreateCategoryAliasRequest req) {
        CategoryAlias existing = aliasRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alias not found"));

        String newAlias = req.alias().trim();
        String newCategorySlug = req.categorySlug().trim();

        // If alias changed and new alias is already taken by another row -> conflict
        aliasRepo.findByAliasIgnoreCase(newAlias).ifPresent(found -> {
            if (!found.getId().equals(existing.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Alias already exists");
            }
        });

        Category category = categoryRepo.findBySlug(newCategorySlug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        existing.setAlias(newAlias);
        existing.setCategory(category);
        CategoryAlias saved = aliasRepo.save(existing);
        return toDto(saved);
    }

    @Transactional
    public void deleteById(Long id) {
        CategoryAlias existing = aliasRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alias not found"));
        aliasRepo.delete(existing);
    }

    /* ---------------- helper ---------------- */
    private CategoryAliasResponse toDto(CategoryAlias a) {
        return new CategoryAliasResponse(a.getId(), a.getAlias(), a.getCategory().getSlug());
    }
}
