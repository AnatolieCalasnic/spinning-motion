package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class RecordUseCaseImplTest {

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordUseCaseImpl recordUseCase;

    private CreateRecordRequest createRecordRequest;
    private RecordEntity recordEntity;

    @BeforeEach
    void setUp() {
        createRecordRequest = CreateRecordRequest.builder()
                .title("Greatest Hits")
                .artist("Artist Name")
                .genre("Pop")
                .price(19.99)
                .year(2020)
                .condition("New")
                .quantity(10)
                .build();

        recordEntity = RecordEntity.builder()
                .id(1L)
                .title("Greatest Hits")
                .artist("Artist Name")
                .genre("Pop")
                .price(19.99)
                .year(2020)
                .condition("New")
                .quantity(10)
                .build();
    }

    @Test
    void createRecord_Success() {
        when(recordRepository.existsByTitle(createRecordRequest.getTitle())).thenReturn(false);
        when(recordRepository.save(any(RecordEntity.class))).thenReturn(recordEntity);

        CreateRecordResponse response = recordUseCase.createRecord(createRecordRequest);

        assertNotNull(response);
        assertEquals(recordEntity.getId(), response.getId());
        assertEquals(createRecordRequest.getTitle(), response.getTitle());

        verify(recordRepository).existsByTitle(createRecordRequest.getTitle());
        verify(recordRepository).save(any(RecordEntity.class));
    }

    @Test
    void createRecord_InvalidPrice() {
        createRecordRequest.setPrice(0.0); // Invalid price

        assertThrows(InvalidInputException.class, () -> recordUseCase.createRecord(createRecordRequest));

        verify(recordRepository, never()).save(any(RecordEntity.class));
    }

    @Test
    void createRecord_TitleAlreadyExists() {
        when(recordRepository.existsByTitle(createRecordRequest.getTitle())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> recordUseCase.createRecord(createRecordRequest));

        verify(recordRepository).existsByTitle(createRecordRequest.getTitle());
        verify(recordRepository, never()).save(any(RecordEntity.class));
    }

    @Test
    void getRecord_Success() {
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));

        GetRecordResponse response = recordUseCase.getRecord(new GetRecordRequest(1L));

        assertNotNull(response);
        assertEquals(recordEntity.getId(), response.getId());
        assertEquals(recordEntity.getTitle(), response.getTitle());

        verify(recordRepository).findById(1L);
    }

    @Test
    void getRecord_RecordNotFound() {
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> recordUseCase.getRecord(new GetRecordRequest(1L)));

        verify(recordRepository).findById(1L);
    }

    @Test
    void getAllRecords_Success() {
        List<RecordEntity> recordEntities = Arrays.asList(recordEntity);
        when(recordRepository.findAll()).thenReturn(recordEntities);

        List<GetRecordResponse> responses = recordUseCase.getAllRecords();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(recordEntity.getId(), responses.get(0).getId());
        assertEquals(recordEntity.getTitle(), responses.get(0).getTitle());

        verify(recordRepository).findAll();
    }

    @Test
    void updateRecord_Success() {
        UpdateRecordRequest updateRequest = UpdateRecordRequest.builder()
                .id(1L)
                .title("New Title")
                .artist("New Artist")
                .genre("Rock")
                .price(15.99)
                .year(2021)
                .condition("Used")
                .quantity(5)
                .build();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        when(recordRepository.save(any(RecordEntity.class))).thenReturn(recordEntity);

        UpdateRecordResponse response = recordUseCase.updateRecord(updateRequest);

        assertNotNull(response);
        assertEquals(recordEntity.getId(), response.getId());
        assertEquals(updateRequest.getTitle(), response.getTitle());

        verify(recordRepository).findById(1L);
        verify(recordRepository).save(any(RecordEntity.class));
    }

    @Test
    void updateRecord_RecordNotFound() {
        UpdateRecordRequest updateRequest = UpdateRecordRequest.builder().id(1L).build();
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> recordUseCase.updateRecord(updateRequest));

        verify(recordRepository).findById(1L);
        verify(recordRepository, never()).save(any(RecordEntity.class));
    }

    @Test
    void deleteRecord_Success() {
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));

        assertDoesNotThrow(() -> recordUseCase.deleteRecord(1L));

        verify(recordRepository).findById(1L);
        verify(recordRepository).deleteById(1L);
    }

    @Test
    void deleteRecord_RecordNotFound() {
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> recordUseCase.deleteRecord(1L));

        verify(recordRepository).findById(1L);
        verify(recordRepository, never()).deleteById(anyLong());
    }
}