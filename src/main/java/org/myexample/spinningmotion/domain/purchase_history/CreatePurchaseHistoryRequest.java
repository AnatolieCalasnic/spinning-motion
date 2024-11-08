package org.myexample.spinningmotion.domain.purchase_history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseHistoryRequest {
    private Long userId;
    private Long recordId;
    private Integer quantity;
    private Double price;
    private Double totalAmount;
}
