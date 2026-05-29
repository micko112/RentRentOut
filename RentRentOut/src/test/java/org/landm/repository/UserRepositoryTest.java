package org.landm.repository;

import org.junit.jupiter.api.Test;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private UserRepository userRepository;
    @Test
    void findByEmail_returnUser() {
        User saved = createUser("test@test.com");
        em.clear();

        User found = userRepository.findByEmail("test@test.com");
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(saved.getEmail());
        assertThat(found.getId()).isEqualTo(saved.getId());
    }
    @Test
    void findByEmail_userDoesNotExist_returnsNull() {
        User found = userRepository.findByEmail("nepostoji@test.com");
        assertThat(found).isNull();
    }
    @Test
    void existByEmail_returnTrue(){
        createUser("postoji@gmail.com");
        em.clear();

        assertThat(userRepository.existsByEmail("postoji@gmail.com")).isTrue();
        assertThat(userRepository.existsByEmail("nepostoji@test.com")).isFalse();
    }
    User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("test123");
        user.setFirstname("test");
        user.setLastname("testic");
        user.setCurrency(Currency.RSD);
        Role role = new Role();
        role.setName("USER_" + UUID.randomUUID());
        em.persistAndFlush(role);
        user.setRole(role);
        em.persistAndFlush(user);
        return user;

    }
}
