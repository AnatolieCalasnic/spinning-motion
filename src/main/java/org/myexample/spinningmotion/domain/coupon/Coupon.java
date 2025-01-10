package org.myexample.spinningmotion.domain.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    private Long id;
    private Long userId;
    private String couponCode;
    private Integer discountPercentage;
    private LocalDateTime validUntil;
    private Boolean isUsed;
}