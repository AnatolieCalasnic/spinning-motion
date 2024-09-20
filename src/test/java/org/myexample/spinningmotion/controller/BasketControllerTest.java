package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.myexample.spinningmotion.business.exception.BasketNotFoundException;
import org.myexample.spinningmotion.business.exception.OutOfStockException;
import org.myexample.spinningmotion.business.exception.RecordNotInBasketException;
import org.myexample.spinningmotion.business.interfc.BasketUseCase;
import org.myexample.spinningmotion.domain.basket.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BasketController.class)
class BasketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BasketUseCase basketUseCase;

    @Autowired
    private ObjectMapper objectMapper;

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
    void getBasket_Success() throws Exception {
        when(basketUseCase.getBasket(1L)).thenReturn(getBasketResponse);

        mockMvc.perform(get("/basket/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.items[0].recordId").value(1L))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        verify(basketUseCase, times(1)).getBasket(1L);
    }

    @Test
    void addToBasket_Success() throws Exception {
        doNothing().when(basketUseCase).addToBasket(any(AddToBasketRequest.class));

        mockMvc.perform(post("/basket/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToBasketRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Item added to basket"));

        verify(basketUseCase, times(1)).addToBasket(any(AddToBasketRequest.class));
    }

    @Test
    void updateBasketItemQuantity_Success() throws Exception {
        doNothing().when(basketUseCase).updateBasketItemQuantity(any(UpdateBasketItemQuantityRequest.class));

        mockMvc.perform(put("/basket/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBasketItemQuantityRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Basket item quantity updated"));

        verify(basketUseCase, times(1)).updateBasketItemQuantity(any(UpdateBasketItemQuantityRequest.class));
    }

    @Test
    void removeFromBasket_Success() throws Exception {
        doNothing().when(basketUseCase).removeFromBasket(1L, 1L);

        mockMvc.perform(delete("/basket/1/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item removed from basket"));

        verify(basketUseCase, times(1)).removeFromBasket(1L, 1L);
    }

    @Test
    void clearBasket_Success() throws Exception {
        doNothing().when(basketUseCase).clearBasket(1L);

        mockMvc.perform(delete("/basket/1/clear"))
                .andExpect(status().isOk())
                .andExpect(content().string("Basket cleared"));

        verify(basketUseCase, times(1)).clearBasket(1L);
    }

    @Test
    void handleBasketNotFound() throws Exception {
        when(basketUseCase.getBasket(1L)).thenThrow(new BasketNotFoundException("Basket not found"));

        mockMvc.perform(get("/basket/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Basket not found"));
    }

    @Test
    void handleRecordNotInBasket() throws Exception {
        doThrow(new RecordNotInBasketException(1L, 1L)).when(basketUseCase).removeFromBasket(1L, 1L);

        mockMvc.perform(delete("/basket/1/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Record with ID 1 is not in the basket of user 1"));
    }

    @Test
    void handleOutOfStock() throws Exception {
        doThrow(new OutOfStockException("Record", 5, 3)).when(basketUseCase).addToBasket(any(AddToBasketRequest.class));

        mockMvc.perform(post("/basket/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addToBasketRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("We're sorry, the record 'Record' is currently out of stock. Requested: 5, Available: 3"));
    }
}