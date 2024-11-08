package org.myexample.spinningmotion.persistence;

import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity createTestUser(String email) {
        return UserEntity.builder()
                .fname("John")
                .lname("Doe")
                .email(email)
                .password("password123")
                .address("Test Street")
                .postalCode("1234AB")
                .country("NL")
                .city("Eindhoven")
                .region("Noord Brabant")
                .phonenum("1234567890")
                .isAdmin(false)
                .build();
    }

    @Test
    void save_ValidUser_SavesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");

        // Act
        UserEntity savedUser = userRepository.save(user);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    void findById_ExistingUser_ReturnsUser() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<UserEntity> foundUser = userRepository.findById(user.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    void findById_NonExistingUser_ReturnsEmpty() {
        // Act
        Optional<UserEntity> result = userRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_MultipleUsers_ReturnsAllUsers() {
        // Arrange
        entityManager.persist(createTestUser("user1@test.com"));
        entityManager.persist(createTestUser("user2@test.com"));
        entityManager.flush();

        // Act
        List<UserEntity> users = userRepository.findAll();

        // Assert
        assertEquals(2, users.size());
    }

    @Test
    void deleteById_ExistingUser_DeletesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        entityManager.persist(user);
        entityManager.flush();
        Long userId = user.getId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();

        // Assert
        assertNull(entityManager.find(UserEntity.class, userId));
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // Arrange
        UserEntity user = createTestUser("test@example.com");
        entityManager.persist(user);
        entityManager.flush();

        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }
}