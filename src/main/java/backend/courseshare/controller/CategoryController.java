package backend.courseshare.controller;

import backend.courseshare.dto.category.CategoryResponse;
import backend.courseshare.dto.category.CreateCategoryRequest;
import backend.courseshare.dto.category.UpdateCategoryRequest;
import backend.courseshare.dto.category.SuggestedSlugResponse;
import backend.courseshare.entity.Category;
import backend.courseshare.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Category CRUD + PATCH controller.
 *
 * Uses slug as the external identifier: /api/categories/{slug}
 */
@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * POST /api/categories
     * Create new category. Returns 201 Created with Location header (/api/categories/{slug}).
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest req) {
        Category created = categoryService.createCategory(req.name(), req.slug(), req.description());
        CategoryResponse body = toDto(created);
        URI location = URI.create("/api/categories/" + created.getSlug());
        return ResponseEntity.created(location).body(body);
    }

    /**
     * GET /api/categories
     * List all categories.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        List<CategoryResponse> out = categoryService.listAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(out);
    }

    /**
     * GET /api/categories/{slug}
     * Get single category by slug.
     */
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> get(@PathVariable String slug) {
        Category c = categoryService.findBySlugOrThrow(slug);
        return ResponseEntity.ok(toDto(c));
    }

    /**
     * PUT /api/categories/{slug}
     * Full update (replace). Requires name and slug in the request.
     */
    @PutMapping("/{slug}")
    public ResponseEntity<CategoryResponse> put(
            @PathVariable String slug,
            @Valid @RequestBody UpdateCategoryRequest req) {

        Category updated = categoryService.updateCategory(slug, req.name(), req.slug(), req.description());
        return ResponseEntity.ok(toDto(updated));
    }

    /**
     * PATCH /api/categories/{slug}
     * Partial update — only provided fields are changed.
     * We accept a small inline Patch DTO (fields nullable).
     */

    public static record PatchCategoryRequest(
            @Size(max = 200) String name,
            @Size(max = 200) String slug,
            @Size(max = 2000) String description
    ) {}

    @PatchMapping("/{slug}")
    public ResponseEntity<CategoryResponse> patch(
            @PathVariable String slug,
            @RequestBody PatchCategoryRequest req) {

        Category patched = categoryService.patchCategory(slug, req.name(), req.slug(), req.description());
        return ResponseEntity.ok(toDto(patched));
    }

    /**
     * GET /api/categories/suggest-slug?name=...&max=5
     * Returns a suggested unique slug and a small list of alternative candidates.
     */
    @GetMapping("/suggest-slug")
    public ResponseEntity<SuggestedSlugResponse> suggestSlug(
            @RequestParam("name") String name,
            @RequestParam(value = "max", required = false, defaultValue = "5") int max) {

        SuggestedSlugResponse resp = categoryService.suggestSlug(name, max);
        return ResponseEntity.ok(resp);
    }


    /**
     * DELETE /api/categories/{slug}
     */
    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        categoryService.deleteBySlug(slug);
        return ResponseEntity.noContent().build();
    }

    /* ----------------- Helpers ----------------- */

    private CategoryResponse toDto(Category c) {
        // protect against nulls; createdAt is an Instant on the entity
        Instant createdAt = c.getCreatedAt();
        return new CategoryResponse(c.getSlug(), c.getName(), c.getDescription(), createdAt);
    }


}
