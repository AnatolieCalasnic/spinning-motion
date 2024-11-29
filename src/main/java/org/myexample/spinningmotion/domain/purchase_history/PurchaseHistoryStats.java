package org.myexample.spinningmotion.domain.purchase_history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistoryStats {
    private long totalOrders;
    private double totalRevenue;
    private Map<String, Integer> purchasesByRecord; // For chart data
    private double averageOrderValue;
}