package br.edu.unifip.ecommerceapi.service;

import br.edu.unifip.ecommerceapi.dtos.ListCategoryDto;
import br.edu.unifip.ecommerceapi.models.Category;
import br.edu.unifip.ecommerceapi.models.Product;
import br.edu.unifip.ecommerceapi.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Optional<Category> findById(UUID categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Optional<ListCategoryDto> getById(UUID categoryId) {
        var category = categoryRepository.findById(categoryId);
        var categoryDto = new ListCategoryDto(category.get());
        return Optional.of(categoryDto);
    }

    public List<ListCategoryDto> getAllCategories() {

        return categoryRepository.findAll().stream().map(ListCategoryDto::new).toList();

    }

    public List<ListCategoryDto> findByActiveTrue() {

        return categoryRepository.findByActiveTrue().stream().map(ListCategoryDto::new).toList();

    }

    @Transactional
    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public boolean delete(UUID id) {
        var product = categoryRepository.findById(id);
        if (product.isEmpty()) {
            return false;
        }
        product.get().setActive(false);
        return true;
    }

    public Category partialUpdate(Category category, Map<Object, Object> objectMap) {
        objectMap.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Category.class, (String) key);
            field.setAccessible(true);

            try {
                value = BigDecimal.valueOf((double) value);
            }
            catch(ClassCastException ignored) { }
            ReflectionUtils.setField(field, category, value);
        });
        return categoryRepository.save(category);
    }
}
