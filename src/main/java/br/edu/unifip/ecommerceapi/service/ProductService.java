package br.edu.unifip.ecommerceapi.service;

import br.edu.unifip.ecommerceapi.dtos.ListProductDto;
import br.edu.unifip.ecommerceapi.models.Category;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.swing.text.html.Option;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    public List<ListProductDto> getAllProduct() {
        return productRepository.findAll().stream().map(ListProductDto::new).toList();
    }

    public List<ListProductDto> findByActiveTrue() {
        var produtos = productRepository.findByActiveTrue();
        return produtos.stream().map(ListProductDto::new).toList();
    }

    public Optional<ListProductDto> getById(UUID id) {
        var produto = productRepository.getReferenceById(id);
        var productDto = new ListProductDto(produto);
        return Optional.of(productDto);
    }

    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product save(Product product, UUID categoryId) {
        if (categoryId != null) {
            Category category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);

        }
        return productRepository.save(product);
    }

    @Transactional
    public boolean delete(UUID id) {
        var product = productRepository.findById(id);
        if (product.isEmpty()) {
            return false;
        }
        product.get().setActive(false);
        return true;
    }

    public Product partialUpdate(Product product, Map<Object, Object> objectMap) {
        if (objectMap.containsKey("category")) {
            UUID categoryId = (UUID) objectMap.get("category");
            Category category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found."));
            product.setCategory(category);
            objectMap.remove("category");
        }

        objectMap.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Product.class, (String) key);
            field.setAccessible(true);

            try {
                value = BigDecimal.valueOf((double) value);
            }
            catch(ClassCastException ignored) { }
            ReflectionUtils.setField(field, product, value);
        });
        return productRepository.save(product);
    }
}
