package org.myexample.spinningmotion.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.myexample.spinningmotion.persistence.entity.BasketEntity;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BasketRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BasketRepository basketRepository;

    private UserEntity createTestUser() {
        return UserEntity.builder()
                .fname("Johny")
                .lname("Sack")
                .email("test@example.com")
                .password("12345tth")
                .address("hohoho str")
                .postalCode("zip")
                .country("NL")
                .city("Eindhoven")
                .region("Noord Brabant")
                .phonenum("1234567890")
                .isAdmin(false)
                .build();
    }

    private BasketEntity createTestBasket(UserEntity user) {
        return BasketEntity.builder()
                .userId(user.getId())
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void save_ValidBasket_SavesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);
        entityManager.flush();

        BasketEntity basket = createTestBasket(user);

        // Act
        BasketEntity savedBasket = basketRepository.save(basket);

        // Assert
        assertNotNull(savedBasket.getId());
        assertEquals(user.getId(), savedBasket.getUserId());
    }

    @Test
    void findByUserId_ExistingBasket_ReturnsBasket() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);

        BasketEntity basket = createTestBasket(user);
        entityManager.persist(basket);
        entityManager.flush();

        // Act
        Optional<BasketEntity> foundBasket = basketRepository.findByUserId(user.getId());

        // Assert
        assertTrue(foundBasket.isPresent());
        assertEquals(user.getId(), foundBasket.get().getUserId());
    }

    @Test
    void findByUserId_NonExistingUser_ReturnsEmpty() {
        // Act
        Optional<BasketEntity> result = basketRepository.findByUserId(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

}