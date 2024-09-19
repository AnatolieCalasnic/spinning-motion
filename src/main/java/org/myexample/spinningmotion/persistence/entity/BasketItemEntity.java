package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BasketItemEntity {
    private Long recordId;
    private Integer quantity;
}