package br.edu.unifip.ecommerceapi.repository;

import br.edu.unifip.ecommerceapi.dtos.ListCategoryDto;
import br.edu.unifip.ecommerceapi.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByActiveTrue();
}
