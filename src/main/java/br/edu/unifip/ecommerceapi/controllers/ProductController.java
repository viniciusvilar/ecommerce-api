package br.edu.unifip.ecommerceapi.controllers;

import br.edu.unifip.ecommerceapi.dtos.ListProductDto;
import br.edu.unifip.ecommerceapi.dtos.ProductDto;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.service.ProductService;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
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
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ListProductDto>> getAllProducts() {

        return ResponseEntity.ok().body(productService.getAllProduct());

    }

    @GetMapping("/active")
    public ResponseEntity<List<ListProductDto>> getProductIsActive() {

        return ResponseEntity.ok().body(productService.findByActiveTrue());

    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable UUID id) {

        return ResponseEntity.ok().body(productService.getById(id));

    }

    @PostMapping
    public ResponseEntity<Object> saveProduct(@RequestBody @Valid ProductDto productDto, UriComponentsBuilder uriBuilder) {

        var product = new Product(productDto);
        UUID categoryId = null;

        if (productDto.getIdCategory() != null) {
            categoryId = productDto.getIdCategory();
        }

        var uri = uriBuilder.path("/api/products/{id}").buildAndExpand(product.getId()).toUri();

        return ResponseEntity.created(uri).body(productService.save(product, categoryId));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable UUID id) {
        if (productService.delete(id)) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Product was delected");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");

    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable(value = "id") UUID id, @RequestBody Map<Object, Object> objectMap) {
        Optional<Product> productOptional = productService.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found.");
        }
        productService.partialUpdate(productOptional.get(), objectMap);
        return ResponseEntity.status(HttpStatus.OK).body(productOptional.get());
    }

}
