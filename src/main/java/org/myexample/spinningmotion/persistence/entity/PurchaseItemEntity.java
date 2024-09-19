package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PurchaseItemEntity {
    private Long recordId;
    private Integer quantity;
    private Double price;
}