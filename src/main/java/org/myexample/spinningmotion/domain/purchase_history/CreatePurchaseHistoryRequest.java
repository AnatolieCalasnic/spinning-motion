package org.myexample.spinningmotion.domain.purchase_history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseHistoryRequest {
    private Long userId;
    private List<PurchaseItem> items;
}
