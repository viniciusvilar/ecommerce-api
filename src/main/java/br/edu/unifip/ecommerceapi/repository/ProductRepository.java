package br.edu.unifip.ecommerceapi.repository;

import br.edu.unifip.ecommerceapi.dtos.ListProductDto;
import br.edu.unifip.ecommerceapi.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByActiveTrue();
    Optional<Product> findById(UUID id);
}
