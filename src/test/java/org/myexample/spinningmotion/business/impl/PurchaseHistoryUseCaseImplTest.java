package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.business.impl.purchasehistory.PurchaseHistoryUseCaseImpl;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseHistoryUseCaseImplTest {

    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;
    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private PurchaseHistoryUseCaseImpl purchaseHistoryUseCase;

    private CreatePurchaseHistoryRequest createRequest;
    private PurchaseHistoryEntity purchaseHistoryEntity;
    private RecordEntity recordEntity;

    @BeforeEach
    void setUp() {
        createRequest = CreatePurchaseHistoryRequest.builder()
                .userId(1L)
                .recordId(1L)
                .quantity(2)
                .price(10.0)
                .totalAmount(40.0)
                .build();

        purchaseHistoryEntity = PurchaseHistoryEntity.builder()
                .id(1L)
                .userId(1L)
                .purchaseDate(LocalDateTime.now())
                .status("COMPLETED")
                .totalAmount(40.0)
                .recordId(1L)
                .quantity(2)
                .price(10.0)
                .build();
        recordEntity = RecordEntity.builder()
                .id(1L)
                .quantity(5)
                .build();
    }

    @Test
    void createPurchaseHistory_Success() {
        when(recordRepository.findById(any())).thenReturn(Optional.of(recordEntity));
        when(purchaseHistoryRepository.save(any(PurchaseHistoryEntity.class))).thenReturn(purchaseHistoryEntity);

        CreatePurchaseHistoryResponse response = purchaseHistoryUseCase.createPurchaseHistory(createRequest);

        assertNotNull(response);
        assertEquals(purchaseHistoryEntity.getId(), response.getId());
        assertEquals(20.0, response.getTotalAmount());
        verify(recordRepository).findById(createRequest.getRecordId());
        verify(purchaseHistoryRepository).save(any(PurchaseHistoryEntity.class));
    }
    @Test
    void createPurchaseHistory_ValidatesAllFields() {
        when(recordRepository.findById(any())).thenReturn(Optional.of(recordEntity));
        when(purchaseHistoryRepository.save(any(PurchaseHistoryEntity.class))).thenReturn(purchaseHistoryEntity);

        CreatePurchaseHistoryResponse response = purchaseHistoryUseCase.createPurchaseHistory(createRequest);

        assertNotNull(response);
        assertEquals(purchaseHistoryEntity.getUserId(), response.getUserId());
        assertEquals(purchaseHistoryEntity.getStatus(), response.getStatus());
        assertEquals(purchaseHistoryEntity.getPurchaseDate(), response.getPurchaseDate());
        verify(recordRepository).findById(createRequest.getRecordId());
        verify(purchaseHistoryRepository).save(any(PurchaseHistoryEntity.class));
    }
    @Test
    void getPurchaseHistory_Success() {
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.of(purchaseHistoryEntity));

        GetPurchaseHistoryResponse response = purchaseHistoryUseCase.getPurchaseHistory(new GetPurchaseHistoryRequest(1L));

        assertNotNull(response);
        assertEquals(purchaseHistoryEntity.getId(), response.getId());
        assertEquals(purchaseHistoryEntity.getTotalAmount(), response.getTotalAmount());
        assertEquals(purchaseHistoryEntity.getRecordId(), response.getRecordId());
        assertEquals(purchaseHistoryEntity.getQuantity(), response.getQuantity());
        assertEquals(purchaseHistoryEntity.getPrice(), response.getPrice());

        verify(purchaseHistoryRepository).findById(1L);
    }

    @Test
    void getPurchaseHistory_NotFound() {
        GetPurchaseHistoryRequest request = new GetPurchaseHistoryRequest(1L);
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PurchaseHistoryNotFoundException.class, () -> { purchaseHistoryUseCase.getPurchaseHistory(request);
    });
        verify(purchaseHistoryRepository).findById(1L);
    }

    @Test
    void getAllPurchaseHistories_Success() {
        when(purchaseHistoryRepository.findAllByUserId(1L)).thenReturn(Arrays.asList(purchaseHistoryEntity));

        List<GetPurchaseHistoryResponse> responses = purchaseHistoryUseCase.getAllPurchaseHistories(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(purchaseHistoryEntity.getId(), responses.get(0).getId());
        assertEquals(purchaseHistoryEntity.getRecordId(), responses.get(0).getRecordId());
        assertEquals(purchaseHistoryEntity.getQuantity(), responses.get(0).getQuantity());
        assertEquals(purchaseHistoryEntity.getPrice(), responses.get(0).getPrice());

        verify(purchaseHistoryRepository).findAllByUserId(1L);
    }


    @Test
    void getAllPurchaseHistories_EmptyList() {
        when(purchaseHistoryRepository.findAllByUserId(1L)).thenReturn(Collections.emptyList());

        List<GetPurchaseHistoryResponse> responses = purchaseHistoryUseCase.getAllPurchaseHistories(1L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(purchaseHistoryRepository).findAllByUserId(1L);
    }
    @Test
    void deletePurchaseHistory_Success() {
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.of(purchaseHistoryEntity));

        assertDoesNotThrow(() -> purchaseHistoryUseCase.deletePurchaseHistory(1L));

        verify(purchaseHistoryRepository).findById(1L);
        verify(purchaseHistoryRepository).deleteById(1L);
    }

    @Test
    void deletePurchaseHistory_NotFound() {
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PurchaseHistoryNotFoundException.class, () -> purchaseHistoryUseCase.deletePurchaseHistory(1L));

        verify(purchaseHistoryRepository).findById(1L);
        verify(purchaseHistoryRepository, never()).deleteById(anyLong());
    }
    @Test
    void getAdminDashboardStats_Success() {
        // Setup test data
        PurchaseHistoryEntity purchase1 = PurchaseHistoryEntity.builder()
                .id(1L)
                .totalAmount(100.0)
                .purchaseDate(LocalDateTime.now())
                .build();
        PurchaseHistoryEntity purchase2 = PurchaseHistoryEntity.builder()
                .id(2L)
                .totalAmount(150.0)
                .purchaseDate(LocalDateTime.now().minusDays(1))
                .build();
        List<PurchaseHistoryEntity> allPurchases = Arrays.asList(purchase1, purchase2);
        List<PurchaseHistoryEntity> recentPurchases = Arrays.asList(purchase1);

        // Mock repository calls
        when(purchaseHistoryRepository.count()).thenReturn(2L);
        when(purchaseHistoryRepository.findAll()).thenReturn(allPurchases);
        when(purchaseHistoryRepository.findTop10ByOrderByPurchaseDateDesc()).thenReturn(recentPurchases);

        // Execute
        AdminDashboardStats stats = purchaseHistoryUseCase.getAdminDashboardStats();

        // Verify
        assertNotNull(stats);
        assertEquals(2L, stats.getTotalOrders());
        assertEquals(250.0, stats.getTotalRevenue());
        assertEquals(1, stats.getRecentOrders().size());

        verify(purchaseHistoryRepository).count();
        verify(purchaseHistoryRepository).findAll();
        verify(purchaseHistoryRepository).findTop10ByOrderByPurchaseDateDesc();
    }

    @Test
    void getPurchaseHistoryStats_Success() {
        // Setup test data remains the same
        PurchaseHistoryEntity purchase1 = PurchaseHistoryEntity.builder()
                .id(1L)
                .recordId(1L)
                .totalAmount(100.0)
                .quantity(2)
                .build();
        PurchaseHistoryEntity purchase2 = PurchaseHistoryEntity.builder()
                .id(2L)
                .recordId(1L)
                .totalAmount(150.0)
                .quantity(3)
                .build();
        List<PurchaseHistoryEntity> purchases = Arrays.asList(purchase1, purchase2);

        // Mock repository calls
        when(purchaseHistoryRepository.count()).thenReturn(2L);
        when(purchaseHistoryRepository.findAll()).thenReturn(purchases);

        // Execute
        PurchaseHistoryStats stats = purchaseHistoryUseCase.getPurchaseHistoryStats();

        // Verify results remain the same
        assertNotNull(stats);
        assertEquals(2L, stats.getTotalOrders());
        assertEquals(250.0, stats.getTotalRevenue());
        assertEquals(125.0, stats.getAverageOrderValue());

        Map<String, Integer> purchasesByRecord = stats.getPurchasesByRecord();
        assertNotNull(purchasesByRecord);
        assertEquals(5, purchasesByRecord.get("1")); // Total quantity for recordId 1

        // Update verification to match actual number of calls
        verify(purchaseHistoryRepository, times(4)).findAll(); // Called by calculateTotalRevenue (2x), getPurchasesByRecord, and calculateAverageOrderValue
        verify(purchaseHistoryRepository).count();
    }

    @Test
    void getRecentPurchaseHistories_Success() {
        // Setup test data
        List<PurchaseHistoryEntity> recentPurchases = Arrays.asList(
                PurchaseHistoryEntity.builder()
                        .id(1L)
                        .purchaseDate(LocalDateTime.now())
                        .build(),
                PurchaseHistoryEntity.builder()
                        .id(2L)
                        .purchaseDate(LocalDateTime.now().minusHours(1))
                        .build()
        );

        // Mock repository call
        when(purchaseHistoryRepository.findTop10ByOrderByPurchaseDateDesc())
                .thenReturn(recentPurchases);

        // Execute
        List<GetPurchaseHistoryResponse> result = purchaseHistoryUseCase.getRecentPurchaseHistories(10);

        // Verify
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(purchaseHistoryRepository).findTop10ByOrderByPurchaseDateDesc();
    }

    @Test
    void calculateTotalRevenue_Success() {
        // Setup test data
        List<PurchaseHistoryEntity> purchases = Arrays.asList(
                PurchaseHistoryEntity.builder().totalAmount(100.0).build(),
                PurchaseHistoryEntity.builder().totalAmount(150.0).build()
        );

        // Mock repository call
        when(purchaseHistoryRepository.findAll()).thenReturn(purchases);

        // Execute
        double totalRevenue = purchaseHistoryUseCase.calculateTotalRevenue();

        // Verify
        assertEquals(250.0, totalRevenue);
        verify(purchaseHistoryRepository).findAll();
    }

    @Test
    void countTotalOrders_Success() {
        // Mock repository call
        when(purchaseHistoryRepository.count()).thenReturn(5L);

        // Execute
        long totalOrders = purchaseHistoryUseCase.countTotalOrders();

        // Verify
        assertEquals(5L, totalOrders);
        verify(purchaseHistoryRepository).count();
    }

    @Test
    void getPurchasesByRecord_Success() {
        // Setup test data
        List<PurchaseHistoryEntity> purchases = Arrays.asList(
                PurchaseHistoryEntity.builder()
                        .recordId(1L)
                        .quantity(2)
                        .build(),
                PurchaseHistoryEntity.builder()
                        .recordId(1L)
                        .quantity(3)
                        .build(),
                PurchaseHistoryEntity.builder()
                        .recordId(2L)
                        .quantity(1)
                        .build()
        );

        // Mock repository call
        when(purchaseHistoryRepository.findAll()).thenReturn(purchases);

        // Execute
        Map<String, Integer> purchasesByRecord = purchaseHistoryUseCase.getPurchasesByRecord();

        // Verify
        assertNotNull(purchasesByRecord);
        assertEquals(5, purchasesByRecord.get("1")); // 2 + 3 purchases for recordId 1
        assertEquals(1, purchasesByRecord.get("2")); // 1 purchase for recordId 2
        verify(purchaseHistoryRepository).findAll();
    }

    @Test
    void getAllPurchaseHistories_WithoutUserId_Success() {
        // Setup test data
        List<PurchaseHistoryEntity> purchases = Arrays.asList(
                PurchaseHistoryEntity.builder()
                        .id(1L)
                        .userId(1L)
                        .build(),
                PurchaseHistoryEntity.builder()
                        .id(2L)
                        .userId(2L)
                        .build()
        );

        // Mock repository call
        when(purchaseHistoryRepository.findAll()).thenReturn(purchases);

        // Execute
        List<GetPurchaseHistoryResponse> results = purchaseHistoryUseCase.getAllPurchaseHistories();

        // Verify
        assertNotNull(results);
        assertEquals(2, results.size());
        verify(purchaseHistoryRepository).findAll();
    }

    @Test
    void getPurchaseHistoryStats_EmptyPurchases() {
        // Mock repository calls
        when(purchaseHistoryRepository.count()).thenReturn(0L);
        when(purchaseHistoryRepository.findAll()).thenReturn(Collections.emptyList());

        // Execute
        PurchaseHistoryStats stats = purchaseHistoryUseCase.getPurchaseHistoryStats();

        // Verify results remain the same
        assertNotNull(stats);
        assertEquals(0L, stats.getTotalOrders());
        assertEquals(0.0, stats.getTotalRevenue());
        assertEquals(0.0, stats.getAverageOrderValue());
        assertTrue(stats.getPurchasesByRecord().isEmpty());

        // Update verification to match actual number of calls
        verify(purchaseHistoryRepository, times(3)).findAll(); // Called by calculateTotalRevenue, getPurchasesByRecord, and calculateAverageOrderValue
        verify(purchaseHistoryRepository).count();
    }
}