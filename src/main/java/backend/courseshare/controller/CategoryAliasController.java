package backend.courseshare.controller;

import backend.courseshare.dto.alias.CategoryAliasResponse;
import backend.courseshare.dto.alias.CreateCategoryAliasRequest;
import backend.courseshare.service.CategoryAliasService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/category-aliases")
@Validated
public class CategoryAliasController {

    private final CategoryAliasService aliasService;

    public CategoryAliasController(CategoryAliasService aliasService) {
        this.aliasService = aliasService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryAliasResponse> create(@Valid @RequestBody CreateCategoryAliasRequest req) {
        CategoryAliasResponse created = aliasService.createAlias(req);
        URI location = URI.create("/api/category-aliases/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public ResponseEntity<List<CategoryAliasResponse>> listAll() {
        return ResponseEntity.ok(aliasService.listAll());
    }

    @GetMapping("/by-category/{slug}")
    public ResponseEntity<List<CategoryAliasResponse>> listByCategory(@PathVariable String slug) {
        return ResponseEntity.ok(aliasService.listByCategorySlug(slug));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryAliasResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(aliasService.findByIdOrThrow(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryAliasResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody CreateCategoryAliasRequest req) {
        return ResponseEntity.ok(aliasService.updateAlias(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        aliasService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin-test")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminTest() {
        return "Hello Admin!";
    }


}
