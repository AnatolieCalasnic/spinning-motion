package org.myexample.spinningmotion.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemEntity {
    private Long recordId;
    private Integer quantity;
}