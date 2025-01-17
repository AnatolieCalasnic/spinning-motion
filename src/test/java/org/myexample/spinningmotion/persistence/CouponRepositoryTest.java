package org.myexample.spinningmotion.persistence;


import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.CouponEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CouponRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CouponRepository couponRepository;

    private CouponEntity createTestCoupon(String code, Long userId, Integer discountPercentage, LocalDateTime validUntil) {
        return CouponEntity.builder()
                .couponCode(code)
                .userId(userId)
                .discountPercentage(discountPercentage)
                .validUntil(validUntil)
                .isUsed(false)
                .build();
    }

    @Test
    void save_ValidCoupon_SavesSuccessfully() {
        // Arrange
        CouponEntity coupon = createTestCoupon("TEST123", 1L, 20, LocalDateTime.now().plusDays(7));

        // Act
        CouponEntity savedCoupon = couponRepository.save(coupon);

        // Assert
        assertNotNull(savedCoupon.getId());
        assertEquals("TEST123", savedCoupon.getCouponCode());
        assertEquals(1L, savedCoupon.getUserId());
        assertEquals(20, savedCoupon.getDiscountPercentage());
        assertFalse(savedCoupon.getIsUsed());
        assertNotNull(savedCoupon.getCreatedAt());
    }

    @Test
    void findByCouponCode_ExistingCode_ReturnsCoupon() {
        // Arrange
        CouponEntity coupon = createTestCoupon("SAVE20", 1L, 20, LocalDateTime.now().plusDays(7));
        entityManager.persist(coupon);
        entityManager.flush();

        // Act
        Optional<CouponEntity> foundCoupon = couponRepository.findByCouponCode("SAVE20");

        // Assert
        assertTrue(foundCoupon.isPresent());
        assertEquals("SAVE20", foundCoupon.get().getCouponCode());
    }

    @Test
    void findByCouponCode_NonExistingCode_ReturnsEmpty() {
        // Act
        Optional<CouponEntity> foundCoupon = couponRepository.findByCouponCode("NONEXISTENT");

        // Assert
        assertTrue(foundCoupon.isEmpty());
    }

    @Test
    void findByUserId_ExistingUser_ReturnsCoupons() {
        // Arrange
        Long userId = 1L;
        CouponEntity coupon1 = createTestCoupon("CODE1", userId, 20, LocalDateTime.now().plusDays(7));
        CouponEntity coupon2 = createTestCoupon("CODE2", userId, 30, LocalDateTime.now().plusDays(14));
        entityManager.persist(coupon1);
        entityManager.persist(coupon2);
        entityManager.flush();

        // Act
        List<CouponEntity> coupons = couponRepository.findByUserId(userId);

        // Assert
        assertEquals(2, coupons.size());
        assertTrue(coupons.stream().anyMatch(c -> c.getCouponCode().equals("CODE1")));
        assertTrue(coupons.stream().anyMatch(c -> c.getCouponCode().equals("CODE2")));
    }

    @Test
    void existsByUserIdAndIsUsedFalseAndValidUntilAfter_ValidCouponExists_ReturnsTrue() {
        // Arrange
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponEntity validCoupon = createTestCoupon("VALID", userId, 20, now.plusDays(7));
        entityManager.persist(validCoupon);
        entityManager.flush();

        // Act
        boolean exists = couponRepository.existsByUserIdAndIsUsedFalseAndValidUntilAfter(userId, now);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUserIdAndIsUsedFalseAndValidUntilAfter_NoValidCoupon_ReturnsFalse() {
        // Arrange
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponEntity expiredCoupon = createTestCoupon("EXPIRED", userId, 20, now.minusDays(1));
        entityManager.persist(expiredCoupon);
        entityManager.flush();

        // Act
        boolean exists = couponRepository.existsByUserIdAndIsUsedFalseAndValidUntilAfter(userId, now);

        // Assert
        assertFalse(exists);
    }

    @Test
    void findValidCouponsForUser_HasValidCoupons_ReturnsValidCoupons() {
        // Arrange
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();

        // Create valid coupon
        CouponEntity validCoupon = createTestCoupon("VALID", userId, 20, now.plusDays(7));

        // Create expired coupon
        CouponEntity expiredCoupon = createTestCoupon("EXPIRED", userId, 20, now.minusDays(1));

        // Create used coupon
        CouponEntity usedCoupon = createTestCoupon("USED", userId, 20, now.plusDays(7));

        // Persist all coupons first
        entityManager.persist(validCoupon);
        entityManager.persist(expiredCoupon);
        entityManager.persist(usedCoupon);

        // Then update the used status and flush
        usedCoupon.setIsUsed(true);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<CouponEntity> validCoupons = couponRepository.findValidCouponsForUser(userId, now);

        // Assert
        assertEquals(1, validCoupons.size());
        assertEquals("VALID", validCoupons.get(0).getCouponCode());
    }

    @Test
    void findValidCouponsForUser_NoValidCoupons_ReturnsEmptyList() {
        // Arrange
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponEntity expiredCoupon = createTestCoupon("EXPIRED", userId, 20, now.minusDays(1));
        entityManager.persist(expiredCoupon);
        entityManager.flush();

        // Act
        List<CouponEntity> validCoupons = couponRepository.findValidCouponsForUser(userId, now);

        // Assert
        assertTrue(validCoupons.isEmpty());
    }
}