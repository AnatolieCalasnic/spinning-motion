package org.myexample.spinningmotion.business.interfc;
import org.myexample.spinningmotion.domain.purchase_history.*;

import java.util.List;
import java.util.Map;

public interface PurchaseHistoryUseCase {
    CreatePurchaseHistoryResponse createPurchaseHistory(CreatePurchaseHistoryRequest request);
    GetPurchaseHistoryResponse getPurchaseHistory(GetPurchaseHistoryRequest request);
    List<GetPurchaseHistoryResponse> getAllPurchaseHistories(Long userId);
    void deletePurchaseHistory(Long id);
    AdminDashboardStats getAdminDashboardStats();
    PurchaseHistoryStats getPurchaseHistoryStats();
    List<GetPurchaseHistoryResponse> getRecentPurchaseHistories(int limit);
    double calculateTotalRevenue();
    long countTotalOrders();
    Map<String, Integer> getPurchasesByRecord();
    List<GetPurchaseHistoryResponse> getAllPurchaseHistories();
}