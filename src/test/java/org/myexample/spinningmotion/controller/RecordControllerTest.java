package org.myexample.spinningmotion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.interfc.RecordImageUseCase;
import org.myexample.spinningmotion.business.interfc.SubscriberUseCase;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.junit.jupiter.api.BeforeEach;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RecordControllerTest {
    @Mock
    private RecordUseCase recordUseCase;
    @Mock
    private RecordImageUseCase recordImageUseCase;

    @Mock
    private SubscriberUseCase subscriberUseCase;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordController controller;

    private CreateRecordRequest createRecordRequest;
    private CreateRecordResponse createRecordResponse;
    private GetRecordResponse getRecordResponse;
    private UpdateRecordRequest updateRecordRequest;
    private UpdateRecordResponse updateRecordResponse;
    private List<MultipartFile> emptyFileList;
    private RecordEntity testRecordEntity;

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
        testRecordEntity = RecordEntity.builder()
                .id(1L)
                .title("Test Album")
                .artist("Test Artist")
                .price(11.99)
                .genre(GenreEntity.builder().id(1L).name("Rock").build())
                .build();

        emptyFileList = Collections.emptyList();

    }

    @Test
    void createRecord_Success() {
        when(recordUseCase.createRecord(any())).thenReturn(createRecordResponse);
        when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecordEntity));

        // Execute
        ResponseEntity<CreateRecordResponse> response = controller.createRecord(createRecordRequest, emptyFileList);

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createRecordResponse, response.getBody());

        // Verify all interactions in the correct order
        verify(recordUseCase).createRecord(any());
        verify(recordRepository).findById(1L);
        verify(subscriberUseCase).notifySubscribersOfNewRelease(
                testRecordEntity.getTitle(),
                testRecordEntity.getArtist(),
                testRecordEntity.getPrice(),
                testRecordEntity.getGenre().getName()
        );
        verifyNoInteractions(recordImageUseCase);
    }
    @Test
    void createRecord_WithImages() {
        // Setup mocks
        when(recordUseCase.createRecord(any())).thenReturn(createRecordResponse);
        when(recordRepository.findById(1L)).thenReturn(Optional.of(testRecordEntity));

        List<MultipartFile> mockImages = List.of(mock(MultipartFile.class));

        // Execute
        ResponseEntity<CreateRecordResponse> response = controller.createRecord(createRecordRequest, mockImages);

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // Remove eq() matchers and pass values directly
        verify(recordImageUseCase).uploadMultipleImages(1L, mockImages);
    }

    @Test
    void getRecord_Success() {
        when(recordUseCase.getRecord(any())).thenReturn(getRecordResponse);
        ResponseEntity<GetRecordResponse> response = controller.getRecord(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(getRecordResponse, response.getBody());
    }

    @Test
    void getRecord_NotFound() {
        when(recordUseCase.getRecord(any()))
                .thenThrow(new RecordNotFoundException("Record not found"));
        assertThrows(RecordNotFoundException.class, () -> controller.getRecord(1L));
    }

    @Test
    void getAllRecords_Success() {
        List<GetRecordResponse> records = Arrays.asList(getRecordResponse);
        when(recordUseCase.getAllRecords()).thenReturn(records);
        ResponseEntity<List<GetRecordResponse>> response = controller.getAllRecords();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(records, response.getBody());
    }

    @Test
    void getRecordsByGenre_Success() {
        List<GetRecordResponse> records = Arrays.asList(getRecordResponse);
        when(recordUseCase.getRecordsByGenre("Rock")).thenReturn(records);
        ResponseEntity<List<GetRecordResponse>> response = controller.getRecordsByGenre("Rock");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(records, response.getBody());
    }

    @Test
    void updateRecord_Success() {
        when(recordUseCase.updateRecord(any())).thenReturn(updateRecordResponse);
        ResponseEntity<UpdateRecordResponse> response = controller.updateRecord(1L,updateRecordRequest, emptyFileList);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updateRecordResponse, response.getBody());
    }

    @Test
    void deleteRecord_Success() {
        doNothing().when(recordUseCase).deleteRecord(1L);
        ResponseEntity<String> response = controller.deleteRecord(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Record deleted successfully", response.getBody());
    }

    @Test
    void deleteRecord_NotFound() {
        doThrow(new RecordNotFoundException("Record not found"))
                .when(recordUseCase).deleteRecord(1L);
        assertThrows(RecordNotFoundException.class, () -> controller.deleteRecord(1L));
    }
}
