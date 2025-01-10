package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.coupon.GenerateCouponRequest;
import org.myexample.spinningmotion.domain.coupon.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponUseCase {
    boolean generateFrequentShopperCoupon(GenerateCouponRequest request);
    Optional<Coupon> getCouponByCode(String couponCode);
    List<Coupon> getCouponsByUserId(Long userId);
    boolean markCouponAsUsed(String couponCode);
    boolean validateCoupon(String couponCode);
}