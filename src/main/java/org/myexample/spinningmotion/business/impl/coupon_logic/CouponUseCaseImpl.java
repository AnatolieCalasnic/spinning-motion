package org.myexample.spinningmotion.business.impl.coupon_logic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.interfc.CouponUseCase;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.coupon.GenerateCouponRequest;
import org.myexample.spinningmotion.domain.purchase_history.GetPurchaseHistoryResponse;
import org.myexample.spinningmotion.persistence.CouponRepository;
import org.myexample.spinningmotion.persistence.entity.CouponEntity;
import org.springframework.stereotype.Service;
import org.myexample.spinningmotion.domain.coupon.Coupon;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponUseCaseImpl implements CouponUseCase {
    private final CouponRepository couponRepository;
    private final PurchaseHistoryUseCase purchaseHistoryUseCase;

    @Override
    public boolean generateFrequentShopperCoupon(GenerateCouponRequest request) {
        // Check if user already has valid coupons
        boolean hasValidCoupon = couponRepository
                .existsByUserIdAndIsUsedFalseAndValidUntilAfter(
                        request.getUserId(),
                        LocalDateTime.now()
                );

        if (hasValidCoupon) {
            log.info("User {} already has a valid coupon", request.getUserId());
            return false;
        }

        // Calculate threshold date (30 days ago)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Get purchases in the last 30 days
        List<GetPurchaseHistoryResponse> recentPurchases = purchaseHistoryUseCase
                .getAllPurchaseHistories(request.getUserId())
                .stream()
                .filter(p -> p.getPurchaseDate().isAfter(thirtyDaysAgo))
                .collect(Collectors.toList());

        // Count total completed purchases in timeframe
        long purchaseCount = recentPurchases.size();

        log.info("User {} has made {} purchases in the last 30 days", request.getUserId(), purchaseCount);

        // Check if number of purchases is divisible by 3 (every 3rd purchase)
        if (purchaseCount > 0 && purchaseCount % 3 == 0) {
            // Generate coupon
            String couponCode = "SPIN" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            CouponEntity coupon = CouponEntity.builder()
                    .userId(request.getUserId())
                    .couponCode(couponCode)
                    .discountPercentage(30) // Set default 30% discount
                    .validUntil(LocalDateTime.now().plusMonths(1))
                    .isUsed(false)
                    .build();

            CouponEntity savedCoupon = couponRepository.save(coupon);
            log.info("Generated new coupon {} for user {}", savedCoupon.getCouponCode(), request.getUserId());
            return true;
        }

        return false;
    }

    @Override
    public Optional<Coupon> getCouponByCode(String couponCode) {
        return couponRepository.findByCouponCode(couponCode)
                .map(this::mapToCoupon);
    }

    @Override
    public List<Coupon> getCouponsByUserId(Long userId) {
        return couponRepository.findByUserId(userId)
                .stream()
                .map(this::mapToCoupon)
                .collect(Collectors.toList());
    }

    @Override
    public boolean markCouponAsUsed(String couponCode) {
        Optional<CouponEntity> couponOpt = couponRepository.findByCouponCode(couponCode);
        if (couponOpt.isPresent()) {
            CouponEntity coupon = couponOpt.get();
            coupon.setIsUsed(true);
            couponRepository.save(coupon);
            return true;
        }
        return false;
    }

    @Override
    public boolean validateCoupon(String couponCode) {
        log.info("Validating coupon: {}", couponCode);
        return couponRepository.findByCouponCode(couponCode)
                .map(coupon -> {
                    boolean isValid = !coupon.getIsUsed()
                            && coupon.getValidUntil().isAfter(LocalDateTime.now());
                    log.info("Coupon {} validation result: {}", couponCode, isValid);
                    return isValid;
                })
                .orElse(false);
    }

    private Coupon mapToCoupon(CouponEntity entity) {
        return Coupon.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .couponCode(entity.getCouponCode())
                .discountPercentage(entity.getDiscountPercentage())
                .validUntil(entity.getValidUntil())
                .isUsed(entity.getIsUsed())
                .build();
    }}