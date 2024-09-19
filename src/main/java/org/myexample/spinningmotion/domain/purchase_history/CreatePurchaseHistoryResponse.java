package org.myexample.spinningmotion.domain.purchase_history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseHistoryResponse {
    private Long id;
    private Long userId;
    private LocalDateTime purchaseDate;
    private String status;
    private Double totalAmount;
    private List<PurchaseItem> items;
}