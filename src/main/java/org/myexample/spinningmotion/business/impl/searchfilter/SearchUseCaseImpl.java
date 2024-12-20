package org.myexample.spinningmotion.business.impl.searchfilter;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchUseCaseImpl implements SearchUseCase {
    private final RecordRepository recordRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    @Override
    public List<RecordEntity> searchRecords(String searchTerm) {
        return recordRepository.searchRecords(searchTerm);
    }
    @Override
    public List<PurchaseHistoryEntity> searchOrders(String searchTerm) {
        return purchaseHistoryRepository.searchOrders(searchTerm);
    }
}
