package org.myexample.spinningmotion.persistence;

import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PurchaseHistoryRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PurchaseHistoryRepository purchaseHistoryRepository;

    private UserEntity createTestUser() {
        return UserEntity.builder()
                .fname("John")
                .lname("Doe")
                .email("test@example.com")
                .password("1234566")
                .address("Test Street")
                .postalCode("4124FS")
                .country("NL")
                .city("Rotterdam")
                .region("HOHOH")
                .phonenum("1234567890")
                .isAdmin(false)
                .build();
    }


    private PurchaseHistoryEntity createTestPurchase(UserEntity user) {
        return PurchaseHistoryEntity.builder()
                .userId(user.getId())
                .purchaseDate(LocalDateTime.now())
                .totalAmount(29.99)
                .status("COMPLETED")
                .recordId(1L)
                .quantity(2)
                .price(10.0)
                .build();
    }

    @Test
    void save_ValidPurchase_SavesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);
        entityManager.flush();

        PurchaseHistoryEntity purchase = createTestPurchase(user);

        // Act
        PurchaseHistoryEntity savedPurchase = purchaseHistoryRepository.save(purchase);

        // Assert
        assertNotNull(savedPurchase.getId());
        assertEquals(29.99, savedPurchase.getTotalAmount());
        assertEquals(1L, savedPurchase.getRecordId());
        assertEquals(2, savedPurchase.getQuantity());
        assertEquals(10.0, savedPurchase.getPrice());
    }

    @Test
    void findById_ExistingPurchase_ReturnsPurchase() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);

        PurchaseHistoryEntity purchase = createTestPurchase(user);
        entityManager.persist(purchase);
        entityManager.flush();

        // Act
        Optional<PurchaseHistoryEntity> foundPurchase = purchaseHistoryRepository.findById(purchase.getId());

        // Assert
        assertTrue(foundPurchase.isPresent());
        assertEquals(29.99, foundPurchase.get().getTotalAmount());
        assertEquals(1L, foundPurchase.get().getRecordId());
        assertEquals(2, foundPurchase.get().getQuantity());
        assertEquals(10.0, foundPurchase.get().getPrice());
    }

    @Test
    void findById_NonExistingPurchase_ReturnsEmpty() {
        // Act
        Optional<PurchaseHistoryEntity> result = purchaseHistoryRepository.findById(999L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByUserId_UserWithPurchases_ReturnsAllPurchases() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);

        PurchaseHistoryEntity purchase1 = createTestPurchase(user);
        PurchaseHistoryEntity purchase2 = createTestPurchase(user);
        entityManager.persist(purchase1);
        entityManager.persist(purchase2);
        entityManager.flush();

        // Act
        List<PurchaseHistoryEntity> purchases = purchaseHistoryRepository.findAllByUserId(user.getId());

        // Assert
        assertEquals(2, purchases.size());
    }

    @Test
    void findAllByUserId_UserWithNoPurchases_ReturnsEmptyList() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);
        entityManager.flush();

        // Act
        List<PurchaseHistoryEntity> purchases = purchaseHistoryRepository.findAllByUserId(user.getId());

        // Assert
        assertTrue(purchases.isEmpty());
    }

    @Test
    void deleteById_ExistingPurchase_DeletesSuccessfully() {
        // Arrange
        UserEntity user = createTestUser();
        entityManager.persist(user);

        PurchaseHistoryEntity purchase = createTestPurchase(user);
        entityManager.persist(purchase);
        entityManager.flush();

        Long purchaseId = purchase.getId();

        // Act
        purchaseHistoryRepository.deleteById(purchaseId);
        entityManager.flush();

        // Assert
        assertNull(entityManager.find(PurchaseHistoryEntity.class, purchaseId));
    }
}