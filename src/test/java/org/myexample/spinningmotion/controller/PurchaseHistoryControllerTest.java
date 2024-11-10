package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.AnnotationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseHistoryController.class)
class PurchaseHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseHistoryUseCase purchaseHistoryUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private CreatePurchaseHistoryRequest createRequest;
    private CreatePurchaseHistoryResponse createResponse;
    private GetPurchaseHistoryResponse getPurchaseHistoryResponse;

    @BeforeEach
    void setUp() {


        createRequest = CreatePurchaseHistoryRequest.builder()
                .userId(1L)
                .recordId(1L)
                .quantity(2)
                .price(19.99)
                .totalAmount(39.98)
                .build();

        createResponse = CreatePurchaseHistoryResponse.builder()
                .id(1L)
                .userId(1L)
                .purchaseDate(LocalDateTime.now())
                .status("Completed")
                .totalAmount(39.98)
                .recordId(1L)
                .quantity(2)
                .price(19.99)
                .build();

        getPurchaseHistoryResponse = GetPurchaseHistoryResponse.builder()
                .id(1L)
                .userId(1L)
                .purchaseDate(LocalDateTime.now())
                .status("Completed")
                .totalAmount(39.98)
                .recordId(1L)
                .quantity(2)
                .price(19.99)
                .build();
    }

    @Test
    void createPurchaseHistory_Success() throws Exception {
        when(purchaseHistoryUseCase.createPurchaseHistory(any(CreatePurchaseHistoryRequest.class)))
                .thenReturn(createResponse);

        mockMvc.perform(post("/purchase-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("Completed"))
                .andExpect(jsonPath("$.totalAmount").value(39.98))
                .andExpect(jsonPath("$.recordId").value(1L))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.price").value(19.99));

        verify(purchaseHistoryUseCase, times(1)).createPurchaseHistory(any(CreatePurchaseHistoryRequest.class));
    }

    @Test
    void getAllPurchaseHistories_Success() throws Exception {
        List<GetPurchaseHistoryResponse> responses = Arrays.asList(getPurchaseHistoryResponse);
        when(purchaseHistoryUseCase.getAllPurchaseHistories(1L)).thenReturn(responses);

        mockMvc.perform(get("/purchase-history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].status").value("Completed"))
                .andExpect(jsonPath("$[0].totalAmount").value(39.98))
                .andExpect(jsonPath("$[0].recordId").value(1L))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[0].price").value(19.99));

        verify(purchaseHistoryUseCase, times(1)).getAllPurchaseHistories(1L);
    }

    @Test
    void getPurchaseHistory_Success() throws Exception {
        when(purchaseHistoryUseCase.getPurchaseHistory(any(GetPurchaseHistoryRequest.class)))
                .thenReturn(getPurchaseHistoryResponse);

        mockMvc.perform(get("/purchase-history/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.status").value("Completed"))
                .andExpect(jsonPath("$.totalAmount").value(39.98))
                .andExpect(jsonPath("$.recordId").value(1L))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.price").value(19.99));

        verify(purchaseHistoryUseCase, times(1)).getPurchaseHistory(any(GetPurchaseHistoryRequest.class));
    }
    @Test
    void getAllPurchaseHistories_EmptyList() throws Exception {
        when(purchaseHistoryUseCase.getAllPurchaseHistories(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/purchase-history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(purchaseHistoryUseCase).getAllPurchaseHistories(1L);
    }
    @Test
    void getPurchaseHistory_NotFound() throws Exception {
        when(purchaseHistoryUseCase.getPurchaseHistory(any(GetPurchaseHistoryRequest.class)))
                .thenThrow(new PurchaseHistoryNotFoundException("Purchase history not found"));

        mockMvc.perform(get("/purchase-history/history/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Purchase history not found"));

        verify(purchaseHistoryUseCase, times(1)).getPurchaseHistory(any(GetPurchaseHistoryRequest.class));
    }
    // Add these tests to PurchaseHistoryControllerTest class

    @Test
    void handleAnnotationException_ShouldReturnBadRequest() throws Exception {
        // Given
        when(purchaseHistoryUseCase.createPurchaseHistory(any(CreatePurchaseHistoryRequest.class)))
                .thenThrow(new AnnotationException("Invalid mapping"));

        // When & Then
        mockMvc.perform(post("/purchase-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error in entity mapping: Invalid mapping"));
    }

    @Test
    void handleGeneralException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(purchaseHistoryUseCase.createPurchaseHistory(any(CreatePurchaseHistoryRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When & Then
        mockMvc.perform(post("/purchase-history")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred: Unexpected error"));
    }
    @Test
    void deletePurchaseHistory_Success() throws Exception {
        doNothing().when(purchaseHistoryUseCase).deletePurchaseHistory(1L);

        mockMvc.perform(delete("/purchase-history/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Purchase history deleted"));

        verify(purchaseHistoryUseCase, times(1)).deletePurchaseHistory(1L);
    }

    @Test
    void deletePurchaseHistory_NotFound() throws Exception {
        doThrow(new PurchaseHistoryNotFoundException("Purchase history not found"))
                .when(purchaseHistoryUseCase).deletePurchaseHistory(1L);

        mockMvc.perform(delete("/purchase-history/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Purchase history not found"));

        verify(purchaseHistoryUseCase, times(1)).deletePurchaseHistory(1L);
    }
}