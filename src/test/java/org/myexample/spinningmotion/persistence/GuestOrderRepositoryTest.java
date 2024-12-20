package org.myexample.spinningmotion.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GuestOrderRepositoryTest {

    @Autowired
    private GuestOrderRepository guestOrderRepository;

    @Autowired
    private TestEntityManager entityManager;

    private GuestDetailsEntity testGuestOrder;

    @BeforeEach
    void setUp() {
        testGuestOrder = GuestDetailsEntity.builder()
                .purchaseHistoryId(1L)
                .fname("John")
                .lname("Doe")
                .email("john.doe@example.com")
                .address("123 Test Street")
                .postalCode("12345")
                .country("Test Country")
                .city("Test City")
                .region("Test Region")
                .phonenum("123456789")
                .build();
    }

    @Test
    void testSaveGuestOrder() {
        // When
        GuestDetailsEntity savedGuestOrder = guestOrderRepository.save(testGuestOrder);

        // Then
        assertThat(savedGuestOrder).isNotNull();
        assertThat(savedGuestOrder.getId()).isNotNull();
        assertThat(savedGuestOrder.getFname()).isEqualTo("John");
    }

    @Test
    void testFindFirstByPurchaseHistoryId() {
        // Given
        entityManager.persist(testGuestOrder);
        entityManager.flush();

        // When
        GuestDetailsEntity foundGuestOrder = guestOrderRepository.findFirstByPurchaseHistoryId(1L);

        // Then
        assertThat(foundGuestOrder).isNotNull();
        assertThat(foundGuestOrder.getPurchaseHistoryId()).isEqualTo(1L);
        assertThat(foundGuestOrder.getFname()).isEqualTo("John");
    }

    @Test
    void testFindAllByPurchaseHistoryId() {
        // Given
        GuestDetailsEntity guestOrder1 = testGuestOrder;
        GuestDetailsEntity guestOrder2 = GuestDetailsEntity.builder()
                .purchaseHistoryId(1L)
                .fname("Jane")
                .lname("Smith")
                .email("jane.smith@example.com")
                .address("456 Another Street")
                .postalCode("54321")
                .country("Another Country")
                .city("Another City")
                .region("Another Region")
                .phonenum("987654321")
                .build();

        entityManager.persist(guestOrder1);
        entityManager.persist(guestOrder2);
        entityManager.flush();

        // When
        List<GuestDetailsEntity> guestOrders = guestOrderRepository.findAllByPurchaseHistoryId(1L);

        // Then
        assertThat(guestOrders).isNotNull();
        assertThat(guestOrders).hasSize(2);
        assertThat(guestOrders)
                .extracting(GuestDetailsEntity::getPurchaseHistoryId)
                .containsOnly(1L);
    }

    @Test
    void testFindFirstByPurchaseHistoryId_NotFound() {
        // When
        GuestDetailsEntity foundGuestOrder = guestOrderRepository.findFirstByPurchaseHistoryId(999L);

        // Then
        assertThat(foundGuestOrder).isNull();
    }

    @Test
    void testFindAllByPurchaseHistoryId_EmptyResult() {
        // When
        List<GuestDetailsEntity> guestOrders = guestOrderRepository.findAllByPurchaseHistoryId(999L);

        // Then
        assertThat(guestOrders).isEmpty();
    }

    @Test
    void testUpdateGuestOrder() {
        // Given
        GuestDetailsEntity savedGuestOrder = guestOrderRepository.save(testGuestOrder);

        // When
        savedGuestOrder.setFname("Updated Name");
        GuestDetailsEntity updatedGuestOrder = guestOrderRepository.save(savedGuestOrder);

        // Then
        assertThat(updatedGuestOrder.getFname()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteGuestOrder() {
        // Given
        GuestDetailsEntity savedGuestOrder = guestOrderRepository.save(testGuestOrder);

        // When
        guestOrderRepository.delete(savedGuestOrder);

        // Then
        assertThat(guestOrderRepository.findById(savedGuestOrder.getId())).isEmpty();
    }
}