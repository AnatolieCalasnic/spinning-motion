package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.AnnotationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PurchaseHistoryControllerTest {
    @Mock
    private PurchaseHistoryUseCase purchaseHistoryUseCase;

    @InjectMocks
    private PurchaseHistoryController controller;
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
    void createPurchaseHistory_Success() {
        when(purchaseHistoryUseCase.createPurchaseHistory(any())).thenReturn(createResponse);
        ResponseEntity<CreatePurchaseHistoryResponse> response = controller.createPurchaseHistory(createRequest);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createResponse, response.getBody());
        verify(purchaseHistoryUseCase).createPurchaseHistory(createRequest);
    }

    @Test
    void getAllPurchaseHistories_Success() {
        List<GetPurchaseHistoryResponse> responses = Arrays.asList(getPurchaseHistoryResponse);
        when(purchaseHistoryUseCase.getAllPurchaseHistories(1L)).thenReturn(responses);
        ResponseEntity<List<GetPurchaseHistoryResponse>> response = controller.getAllPurchaseHistories(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responses, response.getBody());
    }

    @Test
    void getPurchaseHistory_Success() {
        when(purchaseHistoryUseCase.getPurchaseHistory(any())).thenReturn(getPurchaseHistoryResponse);
        ResponseEntity<GetPurchaseHistoryResponse> response = controller.getPurchaseHistory(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getPurchaseHistoryResponse, response.getBody());
    }

    @Test
    void getPurchaseHistory_NotFound() {
        when(purchaseHistoryUseCase.getPurchaseHistory(any()))
                .thenThrow(new PurchaseHistoryNotFoundException("Purchase history not found"));
        assertThrows(PurchaseHistoryNotFoundException.class,
                () -> controller.getPurchaseHistory(1L));
    }

    @Test
    void getAllPurchaseHistories_EmptyList() {
        when(purchaseHistoryUseCase.getAllPurchaseHistories(1L)).thenReturn(Collections.emptyList());
        ResponseEntity<List<GetPurchaseHistoryResponse>> response = controller.getAllPurchaseHistories(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void deletePurchaseHistory_Success() {
        doNothing().when(purchaseHistoryUseCase).deletePurchaseHistory(1L);
        ResponseEntity<String> response = controller.deletePurchaseHistory(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Purchase history deleted", response.getBody());
    }

    @Test
    void deletePurchaseHistory_NotFound() {
        doThrow(new PurchaseHistoryNotFoundException("Purchase history not found"))
                .when(purchaseHistoryUseCase).deletePurchaseHistory(1L);
        assertThrows(PurchaseHistoryNotFoundException.class,
                () -> controller.deletePurchaseHistory(1L));
    }
}
