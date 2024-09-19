package org.myexample.spinningmotion.domain.basket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddToBasketRequest {
    private Long userId;
    private Long recordId;
    private Integer quantity;
}

