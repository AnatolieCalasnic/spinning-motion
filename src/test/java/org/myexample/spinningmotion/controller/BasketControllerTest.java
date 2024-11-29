package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.myexample.spinningmotion.business.exception.BasketNotFoundException;
import org.myexample.spinningmotion.business.exception.OutOfStockException;
import org.myexample.spinningmotion.business.exception.RecordNotInBasketException;
import org.myexample.spinningmotion.business.interfc.BasketUseCase;
import org.myexample.spinningmotion.domain.basket.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BasketControllerTest {
    @Mock
    private BasketUseCase basketUseCase;

    @InjectMocks
    private BasketController basketController;

    private GetBasketResponse getBasketResponse;
    private AddToBasketRequest addToBasketRequest;
    private UpdateBasketItemQuantityRequest updateBasketItemQuantityRequest;

    @BeforeEach
    void setUp() {
        getBasketResponse = GetBasketResponse.builder()
                .id(1L)
                .userId(1L)
                .items(Arrays.asList(new BasketItem(1L, 2)))
                .build();

        addToBasketRequest = AddToBasketRequest.builder()
                .userId(1L)
                .recordId(1L)
                .quantity(2)
                .build();

        updateBasketItemQuantityRequest = UpdateBasketItemQuantityRequest.builder()
                .userId(1L)
                .recordId(1L)
                .quantity(3)
                .build();
    }

    @Test
    void getBasket_Success() {
        when(basketUseCase.getBasket(1L)).thenReturn(getBasketResponse);
        ResponseEntity<GetBasketResponse> response = basketController.getBasket(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getBasketResponse, response.getBody());
        verify(basketUseCase).getBasket(1L);
    }

    @Test
    void addToBasket_Success() {
        doNothing().when(basketUseCase).addToBasket(any(AddToBasketRequest.class));
        ResponseEntity<?> response = basketController.addToBasket(addToBasketRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(basketUseCase).addToBasket(any(AddToBasketRequest.class));
    }

    @Test
    void updateBasketItemQuantity_Success() {
        doNothing().when(basketUseCase).updateBasketItemQuantity(any(UpdateBasketItemQuantityRequest.class));
        ResponseEntity<String> response = basketController.updateBasketItemQuantity(updateBasketItemQuantityRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(basketUseCase).updateBasketItemQuantity(any(UpdateBasketItemQuantityRequest.class));
    }

    @Test
    void removeFromBasket_Success() {
        doNothing().when(basketUseCase).removeFromBasket(1L, 1L);
        ResponseEntity<String> response = basketController.removeFromBasket(1L, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(basketUseCase).removeFromBasket(1L, 1L);
    }

    @Test
    void clearBasket_Success() {
        doNothing().when(basketUseCase).clearBasket(1L);
        ResponseEntity<String> response = basketController.clearBasket(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(basketUseCase).clearBasket(1L);
    }
}
