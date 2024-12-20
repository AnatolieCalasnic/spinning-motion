package org.myexample.spinningmotion.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchFilterControllerTest {

    @Mock
    private SearchUseCase searchUseCase;

    @InjectMocks
    private SearchFilterController searchFilterController;

    private RecordEntity record1;
    private RecordEntity record2;
    private PurchaseHistoryEntity purchase1;
    private PurchaseHistoryEntity purchase2;

    @BeforeEach
    void setUp() {
        record1 = new RecordEntity();
        record2 = new RecordEntity();
        purchase1 = new PurchaseHistoryEntity();
        purchase2 = new PurchaseHistoryEntity();
    }

    @Test
    void searchRecords_ReturnsMatchingRecords() {
        // Arrange
        String searchTerm = "test";
        List<RecordEntity> expectedRecords = Arrays.asList(record1, record2);
        when(searchUseCase.searchRecords(searchTerm)).thenReturn(expectedRecords);

        // Act
        ResponseEntity<List<RecordEntity>> response = searchFilterController.searchRecords(searchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedRecords, response.getBody());
        verify(searchUseCase).searchRecords(searchTerm);
    }

    @Test
    void searchOrders_ReturnsMatchingOrders() {
        // Arrange
        String searchTerm = "test";
        List<PurchaseHistoryEntity> expectedPurchases = Arrays.asList(purchase1, purchase2);
        when(searchUseCase.searchOrders(searchTerm)).thenReturn(expectedPurchases);

        // Act
        ResponseEntity<List<PurchaseHistoryEntity>> response = searchFilterController.searchOrders(searchTerm);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedPurchases, response.getBody());
        verify(searchUseCase).searchOrders(searchTerm);
    }
}

