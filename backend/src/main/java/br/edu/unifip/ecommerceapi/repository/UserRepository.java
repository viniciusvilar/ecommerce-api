package br.edu.unifip.ecommerceapi.repository;

import br.edu.unifip.ecommerceapi.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> findByActiveTrue();
}
