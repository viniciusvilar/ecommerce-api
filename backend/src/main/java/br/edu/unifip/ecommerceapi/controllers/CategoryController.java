package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.CategoryDto;
import br.edu.unifip.ecommerceapi.dtos.ListCategoryDto;
import br.edu.unifip.ecommerceapi.models.Category;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<ListCategoryDto>> getAllCategories() {
        return ResponseEntity.ok().body(categoryService.getAllCategories());
    }

    @GetMapping("/active")
    public ResponseEntity<List<ListCategoryDto>> getCategoriesIsActive() {
        return ResponseEntity.ok().body(categoryService.findByActiveTrue());
    }

    /*@GetMapping("/{id}")
    public ResponseEntity<Optional<Category>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(categoryService.findById(id));
    }*/

    @GetMapping("/{id}")
    public ResponseEntity<Optional<ListCategoryDto>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Object> saveCategory(@RequestBody CategoryDto categoryDto, UriComponentsBuilder uriBuilder) {
        var category = new Category(categoryDto);
        var uri = uriBuilder.path("/api/category/{id}").buildAndExpand(category.getId()).toUri();
        return ResponseEntity.created(uri).body(categoryService.save(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable UUID id) {
        if (categoryService.delete(id)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Product was delected");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");

    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, @RequestBody Map<Object, Object> objectMap) {
        Optional<Category> categoryOptional = categoryService.findById(id);
        if (categoryOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        categoryService.partialUpdate(categoryOptional.get(), objectMap);
        return ResponseEntity.status(HttpStatus.OK).body(categoryOptional.get());
    }

}
