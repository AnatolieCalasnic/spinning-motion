package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.util.List;

public interface SearchUseCase {
    List<RecordEntity> searchRecords(String searchTerm);
    List<PurchaseHistoryEntity> searchOrders(String searchTerm);
}