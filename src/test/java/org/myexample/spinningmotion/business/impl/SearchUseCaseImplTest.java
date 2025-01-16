package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

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
    private PurchaseHistoryEntity order1;
    private PurchaseHistoryEntity order2;
    private SearchUseCase searchUseCaseInterface;

    @BeforeEach
    void setUp() {
        record1 = RecordEntity.builder()
                .id(1L)
                .title("Test Record 1")
                .artist("Test Artist 1")
                .build();

        record2 = RecordEntity.builder()
                .id(2L)
                .title("Test Record 2")
                .artist("Test Artist 2")
                .build();
        order1 = PurchaseHistoryEntity.builder()
                .id(1L)
                .userId(101L)
                .recordId(1L)
                .status("COMPLETED")
                .build();

        order2 = PurchaseHistoryEntity.builder()
                .id(2L)
                .userId(102L)
                .recordId(2L)
                .status("PENDING")
                .build();

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
    private static Stream<Arguments> emptySearchTermTestCases() {
        return Stream.of(
                Arguments.of(null, "Null search term returns empty list"),
                Arguments.of("  ", "Empty search term returns empty list"),
                Arguments.of("", "Blank search term returns empty list")
        );
    }

    @ParameterizedTest(name = "searchOrders: {1}")
    @MethodSource("emptySearchTermTestCases")
    void searchOrders_WithEmptySearchTerm_ShouldReturnEmptyList(String searchTerm, String testDescription) {
        // When
        List<PurchaseHistoryEntity> result = searchUseCaseInterface.searchOrders(searchTerm);

        // Then
        assertAll(
                () -> assertTrue(result.isEmpty(),
                        String.format("Result should be empty for search term: '%s'", searchTerm)),
                () -> verifyNoInteractions(purchaseHistoryRepository)
        );
    }
    @Test
    void searchOrders_WithValidTerm_ShouldReturnResults() {
        // Given
        String searchTerm = "test";
        List<PurchaseHistoryEntity> expectedOrders = Arrays.asList(order1, order2);
        when(purchaseHistoryRepository.searchOrders(searchTerm.trim())).thenReturn(expectedOrders);

        // When
        List<PurchaseHistoryEntity> result = searchUseCaseInterface.searchOrders(searchTerm);

        // Then
        assertAll(
                () -> assertEquals(expectedOrders, result,
                        "Should return matching orders for valid search term"),
                () -> verify(purchaseHistoryRepository).searchOrders(searchTerm.trim())
        );
    }

    @Test
    void searchOrders_WithValidTerm_NoResults() {
        // Given
        String searchTerm = "nonexistent";
        when(purchaseHistoryRepository.searchOrders(searchTerm.trim())).thenReturn(Collections.emptyList());

        // When
        List<PurchaseHistoryEntity> result = searchUseCaseInterface.searchOrders(searchTerm);

        // Then
        assertAll(
                () -> assertTrue(result.isEmpty(),
                        "Should return empty list when no matches found"),
                () -> verify(purchaseHistoryRepository).searchOrders(searchTerm.trim())
        );
    }
}