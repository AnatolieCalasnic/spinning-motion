package org.myexample.spinningmotion.domain.coupon;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenerateCouponRequest {
    private Long userId;
    private Integer requiredPurchases;
    private Integer discountPercentage;
    private Long timeFrameDays;
}