package org.myexample.spinningmotion.domain.purchase_history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistory {
    private Long id;
    private Long userId;
    private LocalDateTime purchaseDate;
    private String status;
    private Double totalAmount;
    private Long recordId;
    private Integer quantity;
    private Double price;
}