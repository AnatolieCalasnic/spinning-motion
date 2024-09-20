package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.exception.*;
import org.myexample.spinningmotion.domain.basket.*;
import org.myexample.spinningmotion.persistence.BasketRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.BasketEntity;
import org.myexample.spinningmotion.persistence.entity.BasketItemEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasketUseCaseImplTest {

    @Mock
    private BasketRepository basketRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private BasketUseCaseImpl basketUseCase;

    private BasketEntity basketEntity;
    private RecordEntity recordEntity;

    @BeforeEach
    void setUp() {
        basketEntity = BasketEntity.builder()
                .id(1L)
                .userId(1L)
                .items(new ArrayList<>())
                .build();

        recordEntity = RecordEntity.builder()
                .id(1L)
                .title("Test Record")
                .quantity(10)
                .build();
    }

    @Test
    void getBasket_Success() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));

        GetBasketResponse response = basketUseCase.getBasket(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertTrue(response.getItems().isEmpty());

        verify(basketRepository).findByUserId(1L);
    }

    @Test
    void getBasket_NotFound() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(BasketNotFoundException.class, () -> basketUseCase.getBasket(1L));

        verify(basketRepository).findByUserId(1L);
    }

    @Test
    void addToBasket_NewBasket() {
        AddToBasketRequest request = new AddToBasketRequest(1L, 1L, 2);
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        when(basketRepository.save(any(BasketEntity.class))).thenReturn(basketEntity);

        assertDoesNotThrow(() -> basketUseCase.addToBasket(request));

        verify(basketRepository).findByUserId(1L);
        verify(recordRepository).findById(1L);
        verify(basketRepository).save(any(BasketEntity.class));
        verify(recordRepository).save(recordEntity);
    }

    @Test
    void addToBasket_ExistingBasket() {
        AddToBasketRequest request = new AddToBasketRequest(1L, 1L, 2);
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        when(basketRepository.save(any(BasketEntity.class))).thenReturn(basketEntity);

        assertDoesNotThrow(() -> basketUseCase.addToBasket(request));

        verify(basketRepository).findByUserId(1L);
        verify(recordRepository).findById(1L);
        verify(basketRepository).save(basketEntity);
        verify(recordRepository).save(recordEntity);
    }

    @Test
    void addToBasket_OutOfStock() {
        AddToBasketRequest request = new AddToBasketRequest(1L, 1L, 15);
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));

        assertThrows(OutOfStockException.class, () -> basketUseCase.addToBasket(request));

        verify(basketRepository).findByUserId(1L);
        verify(recordRepository).findById(1L);
        verify(basketRepository, never()).save(any(BasketEntity.class));
    }

    @Test
    void updateBasketItemQuantity_Success() {
        UpdateBasketItemQuantityRequest request = new UpdateBasketItemQuantityRequest(1L, 1L, 5);
        basketEntity.getItems().add(new BasketItemEntity(1L, 2));
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));

        assertDoesNotThrow(() -> basketUseCase.updateBasketItemQuantity(request));

        verify(basketRepository).findByUserId(1L);
        verify(recordRepository).findById(1L);
        verify(basketRepository).save(basketEntity);
    }

    @Test
    void updateBasketItemQuantity_RecordNotInBasket() {
        UpdateBasketItemQuantityRequest request = new UpdateBasketItemQuantityRequest(1L, 1L, 5);
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));

        assertThrows(RecordNotInBasketException.class, () -> basketUseCase.updateBasketItemQuantity(request));

        verify(basketRepository).findByUserId(1L);
        verify(basketRepository, never()).save(any(BasketEntity.class));
    }

    @Test
    void removeFromBasket_Success() {
        basketEntity.getItems().add(new BasketItemEntity(1L, 2));
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));

        assertDoesNotThrow(() -> basketUseCase.removeFromBasket(1L, 1L));

        verify(basketRepository).findByUserId(1L);
        verify(basketRepository).save(basketEntity);
        assertTrue(basketEntity.getItems().isEmpty());
    }

    @Test
    void removeFromBasket_RecordNotInBasket() {
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));

        assertThrows(RecordNotInBasketException.class, () -> basketUseCase.removeFromBasket(1L, 1L));

        verify(basketRepository).findByUserId(1L);
        verify(basketRepository, never()).save(any(BasketEntity.class));
    }

    @Test
    void clearBasket_ExistingBasket() {
        basketEntity.getItems().add(new BasketItemEntity(1L, 2));
        when(basketRepository.findByUserId(1L)).thenReturn(Optional.of(basketEntity));

        assertDoesNotThrow(() -> basketUseCase.clearBasket(1L));

        verify(basketRepository).findByUserId(1L);
        verify(basketRepository).save(basketEntity);
        assertTrue(basketEntity.getItems().isEmpty());
    }
}