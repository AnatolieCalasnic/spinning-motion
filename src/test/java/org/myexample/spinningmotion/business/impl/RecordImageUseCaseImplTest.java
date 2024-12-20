package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.record.RecordImageUseCaseImpl;
import org.myexample.spinningmotion.persistence.RecordImageRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecordImageUseCaseImplTest {

    @Mock
    private RecordImageRepository recordImageRepository;

    @Mock
    private RecordRepository recordRepository;

    @InjectMocks
    private RecordImageUseCaseImpl recordImageUseCase;

    private RecordEntity recordEntity;
    private RecordImageEntity imageEntity;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        recordEntity = RecordEntity.builder()
                .id(1L)
                .title("Test Record")
                .build();

        imageEntity = RecordImageEntity.builder()
                .id(1L)
                .record(recordEntity)
                .imageData("test-image".getBytes())
                .imageType("image/jpeg")
                .build();

        multipartFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test-image".getBytes()
        );
    }

    @Test
    void uploadImage_Success() {
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        when(recordImageRepository.save(any(RecordImageEntity.class))).thenReturn(imageEntity);

        RecordImageEntity result = recordImageUseCase.uploadImage(1L, multipartFile);

        assertNotNull(result);
        assertEquals(imageEntity.getId(), result.getId());
        assertEquals(imageEntity.getImageType(), result.getImageType());

        verify(recordRepository).findById(1L);
        verify(recordImageRepository).save(any(RecordImageEntity.class));
    }

    @Test
    void uploadImage_RecordNotFound() {
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadImage(1L, multipartFile)
        );

        verify(recordRepository).findById(1L);
        verify(recordImageRepository, never()).save(any());
    }

    @Test
    void uploadImage_IOExceptionHandling() throws IOException {
        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getBytes()).thenThrow(new IOException("Test exception"));

        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadImage(1L, badFile)
        );

        verify(recordRepository).findById(1L);
        verify(recordImageRepository, never()).save(any());
    }

    @Test
    void getImage_Success() {
        when(recordImageRepository.findById(1L)).thenReturn(Optional.of(imageEntity));

        RecordImageEntity result = recordImageUseCase.getImage(1L);

        assertNotNull(result);
        assertEquals(imageEntity.getId(), result.getId());
        assertEquals(imageEntity.getImageType(), result.getImageType());

        verify(recordImageRepository).findById(1L);
    }

    @Test
    void getImage_NotFound() {
        when(recordImageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.getImage(1L)
        );

        verify(recordImageRepository).findById(1L);
    }

    @Test
    void getImagesByRecordId_Success() {
        List<RecordImageEntity> images = Arrays.asList(imageEntity);
        when(recordImageRepository.findByRecordId(1L)).thenReturn(images);

        List<RecordImageEntity> result = recordImageUseCase.getImagesByRecordId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(imageEntity.getId(), result.get(0).getId());

        verify(recordImageRepository).findByRecordId(1L);
    }

    @Test
    void getImagesByRecordId_EmptyList() {
        when(recordImageRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());

        List<RecordImageEntity> result = recordImageUseCase.getImagesByRecordId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(recordImageRepository).findByRecordId(1L);
    }

    @Test
    void deleteImage_Success() {
        when(recordImageRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> recordImageUseCase.deleteImage(1L));

        verify(recordImageRepository).existsById(1L);
        verify(recordImageRepository).deleteById(1L);
    }

    @Test
    void deleteImage_NotFound() {
        when(recordImageRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.deleteImage(1L)
        );

        verify(recordImageRepository).existsById(1L);
        verify(recordImageRepository, never()).deleteById(any());
    }

    @Test
    void deleteAllImagesForRecord_Success() {
        assertDoesNotThrow(() -> recordImageUseCase.deleteAllImagesForRecord(1L));
        verify(recordImageRepository).deleteByRecordId(1L);
    }
}