package org.myexample.spinningmotion.business.interfc;
import org.myexample.spinningmotion.domain.purchase_history.*;

import java.util.List;

public interface PurchaseHistoryUseCase {
    CreatePurchaseHistoryResponse createPurchaseHistory(CreatePurchaseHistoryRequest request);
    GetPurchaseHistoryResponse getPurchaseHistory(GetPurchaseHistoryRequest request);
    List<GetPurchaseHistoryResponse> getAllPurchaseHistories(Long userId);
    void deletePurchaseHistory(Long id);

}