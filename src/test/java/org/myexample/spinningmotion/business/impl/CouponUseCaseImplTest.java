package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.coupon_logic.CouponUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.coupon.Coupon;
import org.myexample.spinningmotion.domain.coupon.GenerateCouponRequest;
import org.myexample.spinningmotion.domain.purchase_history.GetPurchaseHistoryResponse;
import org.myexample.spinningmotion.persistence.CouponRepository;
import org.myexample.spinningmotion.persistence.entity.CouponEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponUseCaseImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private PurchaseHistoryUseCase purchaseHistoryUseCase;

    @InjectMocks
    private CouponUseCaseImpl couponUseCase;

    private CouponEntity validCouponEntity;
    private CouponEntity usedCouponEntity;
    private CouponEntity expiredCouponEntity;
    private LocalDateTime now;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        // Setup a valid coupon
        validCouponEntity = CouponEntity.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .couponCode("SPIN12345")
                .discountPercentage(30)
                .validUntil(now.plusDays(7))
                .isUsed(false)
                .build();

        // Setup a used coupon
        usedCouponEntity = CouponEntity.builder()
                .id(2L)
                .userId(TEST_USER_ID)
                .couponCode("SPINUSED")
                .discountPercentage(30)
                .validUntil(now.plusDays(7))
                .isUsed(true)
                .build();

        // Setup an expired coupon
        expiredCouponEntity = CouponEntity.builder()
                .id(3L)
                .userId(TEST_USER_ID)
                .couponCode("SPINEXP")
                .discountPercentage(30)
                .validUntil(now.minusDays(1))
                .isUsed(false)
                .build();
    }

    @Test
    void generateFrequentShopperCoupon_UserHasValidCoupon_ReturnsFalse() {
        // Given
        GenerateCouponRequest request = GenerateCouponRequest.builder()
                .userId(TEST_USER_ID)
                .build();

        when(couponRepository.existsByUserIdAndIsUsedFalseAndValidUntilAfter(
                eq(TEST_USER_ID), any(LocalDateTime.class))).thenReturn(true);

        // When
        boolean result = couponUseCase.generateFrequentShopperCoupon(request);

        // Then
        assertFalse(result);
        verify(couponRepository).existsByUserIdAndIsUsedFalseAndValidUntilAfter(
                eq(TEST_USER_ID), any(LocalDateTime.class));
        verifyNoMoreInteractions(couponRepository);
    }

    @Test
    void generateFrequentShopperCoupon_ThreePurchases_ReturnsTrue() {
        // Given
        GenerateCouponRequest request = GenerateCouponRequest.builder()
                .userId(TEST_USER_ID)
                .build();

        List<GetPurchaseHistoryResponse> purchases = Arrays.asList(
                createPurchaseHistory(now.minusDays(1)),
                createPurchaseHistory(now.minusDays(2)),
                createPurchaseHistory(now.minusDays(3))
        );

        when(couponRepository.existsByUserIdAndIsUsedFalseAndValidUntilAfter(
                eq(TEST_USER_ID), any(LocalDateTime.class))).thenReturn(false);
        when(purchaseHistoryUseCase.getAllPurchaseHistories(TEST_USER_ID)).thenReturn(purchases);
        when(couponRepository.save(any(CouponEntity.class))).thenReturn(validCouponEntity);

        // When
        boolean result = couponUseCase.generateFrequentShopperCoupon(request);

        // Then
        assertTrue(result);
        verify(couponRepository).save(any(CouponEntity.class));
    }

    @Test
    void generateFrequentShopperCoupon_LessThanThreePurchases_ReturnsFalse() {
        // Given
        GenerateCouponRequest request = GenerateCouponRequest.builder()
                .userId(TEST_USER_ID)
                .build();

        List<GetPurchaseHistoryResponse> purchases = Arrays.asList(
                createPurchaseHistory(now.minusDays(1)),
                createPurchaseHistory(now.minusDays(2))
        );

        when(couponRepository.existsByUserIdAndIsUsedFalseAndValidUntilAfter(
                eq(TEST_USER_ID), any(LocalDateTime.class))).thenReturn(false);
        when(purchaseHistoryUseCase.getAllPurchaseHistories(TEST_USER_ID)).thenReturn(purchases);

        // When
        boolean result = couponUseCase.generateFrequentShopperCoupon(request);

        // Then
        assertFalse(result);
        verify(couponRepository, never()).save(any(CouponEntity.class));
    }

    @Test
    void getCouponByCode_ValidCoupon_ReturnsOptionalCoupon() {
        // Given
        when(couponRepository.findByCouponCode("SPIN12345")).thenReturn(Optional.of(validCouponEntity));

        // When
        Optional<Coupon> result = couponUseCase.getCouponByCode("SPIN12345");

        // Then
        assertTrue(result.isPresent());
        assertEquals(validCouponEntity.getCouponCode(), result.get().getCouponCode());
        assertEquals(validCouponEntity.getDiscountPercentage(), result.get().getDiscountPercentage());
    }

    @Test
    void getCouponByCode_InvalidCoupon_ReturnsEmptyOptional() {
        // Given
        when(couponRepository.findByCouponCode("INVALID")).thenReturn(Optional.empty());

        // When
        Optional<Coupon> result = couponUseCase.getCouponByCode("INVALID");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getCouponsByUserId_HasCoupons_ReturnsList() {
        // Given
        List<CouponEntity> coupons = Arrays.asList(validCouponEntity, usedCouponEntity);
        when(couponRepository.findByUserId(TEST_USER_ID)).thenReturn(coupons);

        // When
        List<Coupon> result = couponUseCase.getCouponsByUserId(TEST_USER_ID);

        // Then
        assertEquals(2, result.size());
        assertEquals(validCouponEntity.getCouponCode(), result.get(0).getCouponCode());
    }

    @Test
    void markCouponAsUsed_ValidCoupon_ReturnsTrue() {
        // Given
        when(couponRepository.findByCouponCode("SPIN12345")).thenReturn(Optional.of(validCouponEntity));
        when(couponRepository.save(any(CouponEntity.class))).thenReturn(validCouponEntity);

        // When
        boolean result = couponUseCase.markCouponAsUsed("SPIN12345");

        // Then
        assertTrue(result);
        verify(couponRepository).save(any(CouponEntity.class));
    }

    @Test
    void markCouponAsUsed_InvalidCoupon_ReturnsFalse() {
        // Given
        when(couponRepository.findByCouponCode("INVALID")).thenReturn(Optional.empty());

        // When
        boolean result = couponUseCase.markCouponAsUsed("INVALID");

        // Then
        assertFalse(result);
        verify(couponRepository, never()).save(any(CouponEntity.class));
    }

    @Test
    void validateCoupon_ValidCoupon_ReturnsTrue() {
        // Given
        when(couponRepository.findByCouponCode("SPIN12345")).thenReturn(Optional.of(validCouponEntity));

        // When
        boolean result = couponUseCase.validateCoupon("SPIN12345");

        // Then
        assertTrue(result);
    }

    @Test
    void validateCoupon_UsedCoupon_ReturnsFalse() {
        // Given
        when(couponRepository.findByCouponCode("SPINUSED")).thenReturn(Optional.of(usedCouponEntity));

        // When
        boolean result = couponUseCase.validateCoupon("SPINUSED");

        // Then
        assertFalse(result);
    }

    @Test
    void validateCoupon_ExpiredCoupon_ReturnsFalse() {
        // Given
        when(couponRepository.findByCouponCode("SPINEXP")).thenReturn(Optional.of(expiredCouponEntity));

        // When
        boolean result = couponUseCase.validateCoupon("SPINEXP");

        // Then
        assertFalse(result);
    }

    // Helper method to create test purchase history
    private GetPurchaseHistoryResponse createPurchaseHistory(LocalDateTime purchaseDate) {
        return GetPurchaseHistoryResponse.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .purchaseDate(purchaseDate)
                .build();
    }
}