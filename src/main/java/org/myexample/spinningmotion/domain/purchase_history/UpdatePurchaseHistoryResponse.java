package org.myexample.spinningmotion.domain.purchase_history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePurchaseHistoryResponse {
    private Long id;
    private String status;
}