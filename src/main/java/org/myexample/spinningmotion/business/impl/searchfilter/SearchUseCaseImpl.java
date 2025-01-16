package org.myexample.spinningmotion.business.impl.searchfilter;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.SearchException;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SearchUseCaseImpl implements SearchUseCase {
    private static final Logger logger = LoggerFactory.getLogger(SearchUseCaseImpl.class);

    private final RecordRepository recordRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    @Override
    public List<RecordEntity> searchRecords(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmedSearchTerm = searchTerm.trim();
        logger.debug("Performing search with term: {}", trimmedSearchTerm);


        return executeSearch(
                trimmedSearchTerm,
                recordRepository::searchRecords,
                "records"
        );
    }

    @Override
    public List<PurchaseHistoryEntity> searchOrders(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String trimmedSearchTerm = searchTerm.trim();
        logger.debug("Performing order search with term: {}", trimmedSearchTerm);


        return executeSearch(
                trimmedSearchTerm,
                purchaseHistoryRepository::searchOrders,
                "orders"
        );
    }
    private <T> List<T> executeSearch(String searchTerm, Function<String, List<T>> searchFunction, String entityType) {
        Objects.requireNonNull(searchTerm, "Search term must not be null");
        Objects.requireNonNull(searchFunction, "Search function must not be null");
        Objects.requireNonNull(entityType, "Entity type must not be null");

        try {
            List<T> results = searchFunction.apply(searchTerm);
            logger.debug("Found {} {} results for search term: {}", results.size(), entityType, searchTerm);
            return results;
        } catch (RuntimeException e) {
            String errorMsg = String.format("Failed to search %s with term '%s'", entityType, searchTerm);
            logger.error(errorMsg, e);
            throw new SearchException(errorMsg, e);
        }
    }
}
