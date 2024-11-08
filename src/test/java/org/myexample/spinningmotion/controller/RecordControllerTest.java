package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordController.class)
class RecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordUseCase recordUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateRecordRequest createRecordRequest;
    private CreateRecordResponse createRecordResponse;
    private GetRecordResponse getRecordResponse;
    private UpdateRecordRequest updateRecordRequest;
    private UpdateRecordResponse updateRecordResponse;

    @BeforeEach
    void setUp() {
        createRecordRequest = CreateRecordRequest.builder()
                .title("Test Album")
                .artist("Test Artist")
                .genreId(1L)
                .price(11.99)
                .year(2023)
                .condition("New")
                .quantity(10)
                .build();

        createRecordResponse = CreateRecordResponse.builder()
                .id(1L)
                .title("Test Album")
                .artist("Test Artist")
                .genreId(1L)
                .price(11.99)
                .year(2023)
                .condition("New")
                .quantity(10)
                .build();

        getRecordResponse = GetRecordResponse.builder()
                .id(1L)
                .title("Test Album")
                .artist("Test Artist")
                .genreId(1L)
                .price(25.99)
                .year(2023)
                .condition("New")
                .quantity(10)
                .build();

        updateRecordRequest = UpdateRecordRequest.builder()
                .id(1L)
                .title("Updated Album")
                .artist("Updated Artist")
                .genreId(3L)
                .price(29.99)
                .year(2024)
                .condition("Used")
                .quantity(5)
                .build();

        updateRecordResponse = UpdateRecordResponse.builder()
                .id(1L)
                .title("Updated Album")
                .artist("Updated Artist")
                .genreId(3L)
                .price(34.99)
                .year(2024)
                .condition("Used")
                .quantity(5)
                .build();
    }

    @Test
    void createRecord_Success() throws Exception {
        when(recordUseCase.createRecord(any(CreateRecordRequest.class))).thenReturn(createRecordResponse);

        mockMvc.perform(post("/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRecordRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Album"))
                .andExpect(jsonPath("$.artist").value("Test Artist"))
                .andExpect(jsonPath("$.genreId").value(1L))
                .andExpect(jsonPath("$.price").value(11.99))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.condition").value("New"))
                .andExpect(jsonPath("$.quantity").value(10));

        verify(recordUseCase, times(1)).createRecord(any(CreateRecordRequest.class));
    }

    @Test
    void getRecord_Success() throws Exception {
        when(recordUseCase.getRecord(any(GetRecordRequest.class))).thenReturn(getRecordResponse);

        mockMvc.perform(get("/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Album"))
                .andExpect(jsonPath("$.artist").value("Test Artist"))
                .andExpect(jsonPath("$.genreId").value(1L))
                .andExpect(jsonPath("$.price").value(25.99))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.condition").value("New"))
                .andExpect(jsonPath("$.quantity").value(10));

        verify(recordUseCase, times(1)).getRecord(any(GetRecordRequest.class));
    }

    @Test
    void getRecord_NotFound() throws Exception {
        when(recordUseCase.getRecord(any(GetRecordRequest.class)))
                .thenThrow(new RecordNotFoundException("Record not found"));

        mockMvc.perform(get("/records/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Record not found"));

        verify(recordUseCase, times(1)).getRecord(any(GetRecordRequest.class));
    }

    @Test
    void getAllRecords_Success() throws Exception {
        List<GetRecordResponse> records = Arrays.asList(getRecordResponse);
        when(recordUseCase.getAllRecords()).thenReturn(records);

        mockMvc.perform(get("/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Album"))
                .andExpect(jsonPath("$[0].artist").value("Test Artist"))
                .andExpect(jsonPath("$[0].genreId").value(1L))
                .andExpect(jsonPath("$[0].price").value(25.99))
                .andExpect(jsonPath("$[0].year").value(2023))
                .andExpect(jsonPath("$[0].condition").value("New"))
                .andExpect(jsonPath("$[0].quantity").value(10));

        verify(recordUseCase, times(1)).getAllRecords();
    }
    @Test
    void getRecordsByGenre_Success() throws Exception {
        // Arrange
        List<GetRecordResponse> records = Arrays.asList(getRecordResponse);
        when(recordUseCase.getRecordsByGenre("Rock")).thenReturn(records);

        // Act & Assert
        mockMvc.perform(get("/records/genre/Rock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Album"))
                .andExpect(jsonPath("$[0].artist").value("Test Artist"))
                .andExpect(jsonPath("$[0].genreId").value(1L))
                .andExpect(jsonPath("$[0].price").value(25.99))
                .andExpect(jsonPath("$[0].year").value(2023))
                .andExpect(jsonPath("$[0].condition").value("New"))
                .andExpect(jsonPath("$[0].quantity").value(10));

        verify(recordUseCase, times(1)).getRecordsByGenre("Rock");
    }

    @Test
    void getRecordsByGenre_EmptyList() throws Exception {
        // Arrange
        when(recordUseCase.getRecordsByGenre("NonExistentGenre"))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/records/genre/NonExistentGenre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(recordUseCase, times(1)).getRecordsByGenre("NonExistentGenre");
    }

    @Test
    void getRecordsByGenre_ThrowsException() throws Exception {
        // Arrange
        when(recordUseCase.getRecordsByGenre(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/records/genre/Rock"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An error occurred: Unexpected error"));

        verify(recordUseCase, times(1)).getRecordsByGenre("Rock");
    }
    @Test
    void updateRecord_Success() throws Exception {
        when(recordUseCase.updateRecord(any(UpdateRecordRequest.class))).thenReturn(updateRecordResponse);

        mockMvc.perform(put("/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRecordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Album"))
                .andExpect(jsonPath("$.artist").value("Updated Artist"))
                .andExpect(jsonPath("$.genreId").value(3L))
                .andExpect(jsonPath("$.price").value(34.99))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.condition").value("Used"))
                .andExpect(jsonPath("$.quantity").value(5));

        verify(recordUseCase, times(1)).updateRecord(any(UpdateRecordRequest.class));
    }

    @Test
    void deleteRecord_Success() throws Exception {
        doNothing().when(recordUseCase).deleteRecord(1L);

        mockMvc.perform(delete("/records/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Record deleted successfully"));

        verify(recordUseCase, times(1)).deleteRecord(1L);
    }

    @Test
    void deleteRecord_NotFound() throws Exception {
        doThrow(new RecordNotFoundException("Record not found")).when(recordUseCase).deleteRecord(1L);

        mockMvc.perform(delete("/records/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Record not found"));

        verify(recordUseCase, times(1)).deleteRecord(1L);
    }
}