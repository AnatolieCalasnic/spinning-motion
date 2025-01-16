package org.myexample.spinningmotion.persistence;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.SubscriberEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SubscriberRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SubscriberRepository subscriberRepository;

    private SubscriberEntity createTestSubscriber(String email) {
        return SubscriberEntity.builder()
                .email(email)
                .build();
    }

    @Test
    void save_ValidSubscriber_SavesSuccessfully() {
        // Arrange
        SubscriberEntity subscriber = createTestSubscriber("test@example.com");

        // Act
        SubscriberEntity savedSubscriber = subscriberRepository.save(subscriber);

        // Assert
        assertNotNull(savedSubscriber.getId());
        assertEquals("test@example.com", savedSubscriber.getEmail());
        assertNotNull(savedSubscriber.getSubscribedAt());
    }

    @Test
    void save_DuplicateEmail_ThrowsException() {
        // Arrange
        SubscriberEntity subscriber1 = createTestSubscriber("test@example.com");
        subscriberRepository.save(subscriber1);

        SubscriberEntity subscriber2 = createTestSubscriber("test@example.com");

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () ->
                subscriberRepository.saveAndFlush(subscriber2)
        );
    }

    @Test
    void save_InvalidEmail_ThrowsException() {
        // Arrange
        SubscriberEntity subscriber = createTestSubscriber("invalid-email");

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () ->
                entityManager.persistAndFlush(subscriber)
        );
    }

    @Test
    void existsByEmail_ExistingEmail_ReturnsTrue() {
        // Arrange
        SubscriberEntity subscriber = createTestSubscriber("test@example.com");
        entityManager.persist(subscriber);
        entityManager.flush();

        // Act
        boolean exists = subscriberRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_NonExistingEmail_ReturnsFalse() {
        // Act
        boolean exists = subscriberRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByEmail_ExistingEmail_ReturnsSubscriber() {
        // Arrange
        SubscriberEntity subscriber = createTestSubscriber("test@example.com");
        entityManager.persist(subscriber);
        entityManager.flush();

        // Act
        Optional<SubscriberEntity> foundSubscriber = subscriberRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(foundSubscriber.isPresent());
        assertEquals("test@example.com", foundSubscriber.get().getEmail());
    }

    @Test
    void findByEmail_NonExistingEmail_ReturnsEmpty() {
        // Act
        Optional<SubscriberEntity> foundSubscriber = subscriberRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertTrue(foundSubscriber.isEmpty());
    }

    @Test
    void findById_ExistingId_ReturnsSubscriber() {
        // Arrange
        SubscriberEntity subscriber = createTestSubscriber("test@example.com");
        entityManager.persist(subscriber);
        entityManager.flush();

        // Act
        Optional<SubscriberEntity> foundSubscriber = subscriberRepository.findById(subscriber.getId());

        // Assert
        assertTrue(foundSubscriber.isPresent());
        assertEquals("test@example.com", foundSubscriber.get().getEmail());
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        // Act
        Optional<SubscriberEntity> foundSubscriber = subscriberRepository.findById(999L);

        // Assert
        assertTrue(foundSubscriber.isEmpty());
    }

    @Test
    void delete_ExistingSubscriber_DeletesSuccessfully() {
        // Arrange
        SubscriberEntity subscriber = createTestSubscriber("test@example.com");
        entityManager.persist(subscriber);
        entityManager.flush();

        // Act
        subscriberRepository.delete(subscriber);
        entityManager.flush();

        // Assert
        assertFalse(subscriberRepository.existsByEmail("test@example.com"));
    }
}
