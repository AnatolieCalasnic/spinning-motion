package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.CouponUseCase;
import org.myexample.spinningmotion.domain.coupon.GenerateCouponRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.myexample.spinningmotion.domain.coupon.Coupon;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
public class CouponController {
    private final CouponUseCase couponUseCase;

    @PostMapping("/generate")
    public ResponseEntity<Boolean> generateCoupon(@RequestBody GenerateCouponRequest request) {
        return ResponseEntity.ok(couponUseCase.generateFrequentShopperCoupon(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Coupon>> getUserCoupons(@PathVariable Long userId) {
        return ResponseEntity.ok(couponUseCase.getCouponsByUserId(userId));
    }

    @PostMapping("/{couponCode}/use")
    public ResponseEntity<Boolean> useCoupon(@PathVariable String couponCode) {
        return ResponseEntity.ok(couponUseCase.markCouponAsUsed(couponCode));
    }
    @GetMapping("/validate/{couponCode}")
    public ResponseEntity<?> validateCoupon(@PathVariable String couponCode) {
        Optional<Coupon> coupon = couponUseCase.getCouponByCode(couponCode);

        if (coupon.isPresent() && couponUseCase.validateCoupon(couponCode)) {
            return ResponseEntity.ok(coupon.get());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Invalid or expired coupon");
    }
}