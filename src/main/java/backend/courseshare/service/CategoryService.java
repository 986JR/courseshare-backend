package backend.courseshare.service;

import backend.courseshare.entity.Category;
import backend.courseshare.entity.CategoryAlias;
import backend.courseshare.dto.category.SuggestedSlugResponse;
import backend.courseshare.repository.CategoryAliasRepository;
import backend.courseshare.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepo;
    private final CategoryAliasRepository aliasRepo;

    public CategoryService(CategoryRepository categoryRepo, CategoryAliasRepository aliasRepo) {
        this.categoryRepo = categoryRepo;
        this.aliasRepo = aliasRepo;
    }

    /**
     * Resolve an input (name or alias) to a canonical Category, creating it if missing.
     * Used by Course creation flow.
     */
    @Transactional
    public Category resolveOrCreateCategory(String input) {
        if (input == null || input.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category input is required");
        }
        String trimmed = input.trim();

        // Try main category by name
        Optional<Category> main = categoryRepo.findByNameIgnoreCase(trimmed);
        if (main.isPresent()) return main.get();

        // Try alias
        Optional<CategoryAlias> alias = aliasRepo.findByAliasIgnoreCase(trimmed);
        if (alias.isPresent()) return alias.get().getCategory();

        // Create new category
        String slug = toSlug(trimmed);
        // ensure slug uniqueness: if slug exists, append numeric suffix
        slug = uniqueSlugCandidate(slug);

        Category newCategory = new Category();
        newCategory.setName(trimmed);
        newCategory.setSlug(slug);
        // createdAt will be set by @PrePersist in entity
        return categoryRepo.save(newCategory);
    }

    /**
     * List all categories.
     */
    @Transactional(readOnly = true)
    public List<Category> listAll() {
        return categoryRepo.findAll();
    }

    /**
     * Find category by slug (throws 404 if not found).
     */
    @Transactional(readOnly = true)
    public Category findBySlugOrThrow(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slug is required");
        }
        return categoryRepo.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    /**
     * Create a new category explicitly (fails if name or slug already used).
     */
    @Transactional
    public Category createCategory(String name, String maybeSlug, String description) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        String normalizedName = name.trim();

        if (categoryRepo.findByNameIgnoreCase(normalizedName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        String slug = (maybeSlug == null || maybeSlug.isBlank()) ? toSlug(normalizedName) : toSlug(maybeSlug);
        slug = uniqueSlugCandidate(slug);

        Category cat = new Category();
        cat.setName(normalizedName);
        cat.setSlug(slug);
        cat.setDescription(description == null ? null : description.trim());
        return categoryRepo.save(cat);
    }

    /**
     * Full update (PUT) - replace name, slug and description to be unique
     */
    @Transactional
    public Category updateCategory(String slug, String newName, String newSlug, String newDescription) {
        Category existing = findBySlugOrThrow(slug);

        if (newName == null || newName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        String normalizedName = newName.trim();

        // If name changes and is used by another category , conflict
        categoryRepo.findByNameIgnoreCase(normalizedName).ifPresent(other -> {
            if (!other.getId().equals(existing.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already in use");
            }
        });

        String finalSlug = (newSlug == null || newSlug.isBlank()) ? toSlug(normalizedName) : toSlug(newSlug);
        // If slug changed, ensure uniqueness
        if (!finalSlug.equals(existing.getSlug()) && categoryRepo.existsBySlug(finalSlug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category slug already in use");
        }

        existing.setName(normalizedName);
        existing.setSlug(finalSlug);
        existing.setDescription(newDescription == null ? null : newDescription.trim());

        return categoryRepo.save(existing);
    }

    /**
     * Partial update (PATCH) - only update provided fields.
     */
    @Transactional
    public Category patchCategory(String slug, String maybeName, String maybeSlug, String maybeDescription) {
        Category existing = findBySlugOrThrow(slug);

        if (maybeName != null && !maybeName.isBlank()) {
            String normalizedName = maybeName.trim();
            categoryRepo.findByNameIgnoreCase(normalizedName).ifPresent(other -> {
                if (!other.getId().equals(existing.getId())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already in use");
                }
            });
            existing.setName(normalizedName);
        }

        if (maybeSlug != null && !maybeSlug.isBlank()) {
            String candidate = toSlug(maybeSlug);
            if (!candidate.equals(existing.getSlug()) && categoryRepo.existsBySlug(candidate)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category slug already in use");
            }
            existing.setSlug(candidate);
        }

        if (maybeDescription != null) {
            existing.setDescription(maybeDescription.trim());
        }

        return categoryRepo.save(existing);
    }

    /**
     * Delete category by slug. Also deletes aliases since cascade is set on entity.
     */
    @Transactional
    public void deleteBySlug(String slug) {
        Category existing = findBySlugOrThrow(slug);
        categoryRepo.delete(existing);
    }

    /* ----------------- Helpers ----------------- */

    private String uniqueSlugCandidate(String base) {
        String candidate = base;
        int suffix = 1;
        while (categoryRepo.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Create a URL-friendly slug from input like "Computer Science" -> "computer-science".
     */
    private String toSlug(String input) {
        if (input == null) return "category";
        String nowhitespace = WHITESPACE.matcher(input.trim().toLowerCase(Locale.ROOT)).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        // collapse multiple dashes
        slug = slug.replaceAll("-{2,}", "-");
        if (slug.startsWith("-")) slug = slug.substring(1);
        if (slug.endsWith("-")) slug = slug.substring(0, slug.length() - 1);
        if (slug.isBlank()) slug = "category";
        return slug;
    }


    /**
     * Suggest unique slug candidates for a given name.
     * This does NOT reserve or create anything in the DB — it's just a suggestion helper.
     *
     * @param name the human-readable name to create a slug for
     * @param maxCandidates maximum number of suggestions to return (e.g. 5)
     */
    @Transactional(readOnly = true)
    public SuggestedSlugResponse suggestSlug(String name, int maxCandidates) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required for slug suggestion");
        }
        String trimmed = name.trim();

        // 1) if the input matches an alias, suggest the canonical category slug first
        Optional<CategoryAlias> alias = aliasRepo.findByAliasIgnoreCase(trimmed);
        List<String> candidates = new ArrayList<>();

        if (alias.isPresent()) {
            String canonical = alias.get().getCategory().getSlug();
            candidates.add(canonical);
            // If canonical is free (it will be, since it exists), recommend it immediately.
            return new SuggestedSlugResponse(canonical, List.copyOf(candidates));
        }

        // 2) base slug from name
        String base = toSlug(trimmed);
        // collect suggestions: base, base-2, base-3, ...
        candidates.add(base);

        if (!categoryRepo.existsBySlug(base)) {
            // base is available
            return new SuggestedSlugResponse(base, List.copyOf(candidates));
        }

        // base taken — generate further candidates until maxCandidates reached
        int counter = 2;
        while (candidates.size() < Math.max(1, maxCandidates)) {
            String cand = base + "-" + counter;
            candidates.add(cand);
            // short-circuit if available
            if (!categoryRepo.existsBySlug(cand)) {
                return new SuggestedSlugResponse(cand, List.copyOf(candidates));
            }
            counter++;
            // safety: avoid infinite loop (but this will normally terminate quickly)
            if (counter > 1000) break;
        }

        // If none of the generated candidates are free, return the last candidate as fallback
        String fallback = candidates.get(candidates.size() - 1);
        return new SuggestedSlugResponse(fallback, List.copyOf(candidates));
    }

}
