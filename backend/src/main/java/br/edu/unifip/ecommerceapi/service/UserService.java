package br.edu.unifip.ecommerceapi.service;

import br.edu.unifip.ecommerceapi.models.User;
import br.edu.unifip.ecommerceapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findByActiveTrue();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public void hardDelete(User user) {
        userRepository.delete(user);
    }

    @Transactional
    public void softDelete(User user) {
        Optional<User> userInstance = userRepository.findById(user.getId());
        userInstance.ifPresent(value -> value.setActive(false));
    }

    public User partialUpdate(User user, Map<Object, Object> objectMap) {
        objectMap.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(User.class, (String) key);
            field.setAccessible(true);

            try {
                value = BigDecimal.valueOf((double) value);
            } catch (ClassCastException ignored) {
            }
            ReflectionUtils.setField(field, user, value);
        });
        return userRepository.save(user);
    }

    public List<User> findByActiveTrue() {
        return userRepository.findByActiveTrue();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

}
