package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.PurchaseItemEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PurchaseHistoryUseCaseImplTest {

    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;

    @InjectMocks
    private PurchaseHistoryUseCaseImpl purchaseHistoryUseCase;

    private CreatePurchaseHistoryRequest createRequest;
    private PurchaseHistoryEntity purchaseHistoryEntity;

    @BeforeEach
    void setUp() {
        List<PurchaseItem> items = Arrays.asList(
                PurchaseItem.builder().recordId(1L).quantity(2).price(10.0).build(),
                PurchaseItem.builder().recordId(2L).quantity(1).price(20.0).build()
        );

        createRequest = CreatePurchaseHistoryRequest.builder()
                .userId(1L)
                .items(items)
                .build();

        purchaseHistoryEntity = PurchaseHistoryEntity.builder()
                .id(1L)
                .userId(1L)
                .purchaseDate(LocalDateTime.now())
                .status("COMPLETED")
                .totalAmount(40.0)
                .items(Arrays.asList(
                        PurchaseItemEntity.builder().recordId(1L).quantity(2).price(10.0).build(),
                        PurchaseItemEntity.builder().recordId(2L).quantity(1).price(20.0).build()
                ))
                .build();
    }

    @Test
    void createPurchaseHistory_Success() {
        when(purchaseHistoryRepository.save(any(PurchaseHistoryEntity.class))).thenReturn(purchaseHistoryEntity);

        CreatePurchaseHistoryResponse response = purchaseHistoryUseCase.createPurchaseHistory(createRequest);

        assertNotNull(response);
        assertEquals(purchaseHistoryEntity.getId(), response.getId());
        assertEquals(purchaseHistoryEntity.getTotalAmount(), response.getTotalAmount());

        verify(purchaseHistoryRepository).save(any(PurchaseHistoryEntity.class));
    }

    @Test
    void getPurchaseHistory_Success() {
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.of(purchaseHistoryEntity));

        GetPurchaseHistoryResponse response = purchaseHistoryUseCase.getPurchaseHistory(new GetPurchaseHistoryRequest(1L));

        assertNotNull(response);
        assertEquals(purchaseHistoryEntity.getId(), response.getId());
        assertEquals(purchaseHistoryEntity.getTotalAmount(), response.getTotalAmount());

        verify(purchaseHistoryRepository).findById(1L);
    }

    @Test
    void getPurchaseHistory_NotFound() {
        when(purchaseHistoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PurchaseHistoryNotFoundException.class, () -> purchaseHistoryUseCase.getPurchaseHistory(new GetPurchaseHistoryRequest(1L)));

        verify(purchaseHistoryRepository).findById(1L);
    }

    @Test
    void getAllPurchaseHistories_Success() {
        when(purchaseHistoryRepository.findAllByUserId(1L)).thenReturn(Arrays.asList(purchaseHistoryEntity));

        List<GetPurchaseHistoryResponse> responses = purchaseHistoryUseCase.getAllPurchaseHistories(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(purchaseHistoryEntity.getId(), responses.get(0).getId());

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
}