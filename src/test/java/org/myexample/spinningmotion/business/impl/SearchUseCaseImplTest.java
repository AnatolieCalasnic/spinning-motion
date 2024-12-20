package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.searchfilter.SearchUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchUseCaseImplTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;

    @InjectMocks
    private SearchUseCaseImpl searchUseCase;

    private RecordEntity record1;
    private RecordEntity record2;
    private PurchaseHistoryEntity purchase1;
    private PurchaseHistoryEntity purchase2;
    private SearchUseCase searchUseCaseInterface;

    @BeforeEach
    void setUp() {
        record1 = new RecordEntity();
        record2 = new RecordEntity();
        purchase1 = new PurchaseHistoryEntity();
        purchase2 = new PurchaseHistoryEntity();

        // Cast the implementation to the interface
        searchUseCaseInterface = searchUseCase;
    }

    @Test
    void searchRecords_WithResults() {
        // Given
        String searchTerm = "test";
        List<RecordEntity> expectedRecords = Arrays.asList(record1, record2);
        when(recordRepository.searchRecords(searchTerm)).thenReturn(expectedRecords);

        // When
        List<RecordEntity> result = searchUseCaseInterface.searchRecords(searchTerm);

        // Then
        assertEquals(expectedRecords, result);
        verify(recordRepository).searchRecords(searchTerm);
    }

    @Test
    void searchRecords_NoResults() {
        // Given
        String searchTerm = "nonexistent";
        when(recordRepository.searchRecords(searchTerm)).thenReturn(Collections.emptyList());

        // When
        List<RecordEntity> result = searchUseCaseInterface.searchRecords(searchTerm);

        // Then
        assertTrue(result.isEmpty());
        verify(recordRepository).searchRecords(searchTerm);
    }

    @Test
    void searchOrders_WithResults() {
        // Given
        String searchTerm = "test";
        List<PurchaseHistoryEntity> expectedPurchases = Arrays.asList(purchase1, purchase2);
        when(purchaseHistoryRepository.searchOrders(searchTerm)).thenReturn(expectedPurchases);

        // When
        List<PurchaseHistoryEntity> result = searchUseCaseInterface.searchOrders(searchTerm);

        // Then
        assertEquals(expectedPurchases, result);
        verify(purchaseHistoryRepository).searchOrders(searchTerm);
    }

    @Test
    void searchOrders_NoResults() {
        // Given
        String searchTerm = "nonexistent";
        when(purchaseHistoryRepository.searchOrders(searchTerm)).thenReturn(Collections.emptyList());

        // When
        List<PurchaseHistoryEntity> result = searchUseCaseInterface.searchOrders(searchTerm);

        // Then
        assertTrue(result.isEmpty());
        verify(purchaseHistoryRepository).searchOrders(searchTerm);
    }
}