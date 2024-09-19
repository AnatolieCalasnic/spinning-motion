package org.myexample.spinningmotion.domain.basket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBasketResponse {
    private Long id;
    private Long userId;
}