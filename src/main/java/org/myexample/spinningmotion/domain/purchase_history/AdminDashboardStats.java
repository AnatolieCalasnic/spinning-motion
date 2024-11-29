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
public class AdminDashboardStats {
    private long totalOrders;
    private double totalRevenue;
    private int activeUsers; // This might need user service integration
    private List<GetPurchaseHistoryResponse> recentOrders;
}