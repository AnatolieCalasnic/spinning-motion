package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseHistoryEntity {
    private Long id;
    private Long userId;
    private LocalDateTime purchaseDate;
    private String status;
    private Double totalAmount;
    private List<PurchaseItemEntity> items;
}
