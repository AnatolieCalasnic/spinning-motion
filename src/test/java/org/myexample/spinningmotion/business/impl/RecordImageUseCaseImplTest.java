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
import java.util.*;

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
                .images(new ArrayList<>())
                .build();

        imageEntity = RecordImageEntity.builder()
                .id(1L)
                .record(recordEntity)
                .imageData("test-image".getBytes())
                .imageType("image/jpeg")
                .build();
        recordEntity.getImages().add(imageEntity);

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
        when(recordImageRepository.findById(1L)).thenReturn(Optional.of(imageEntity));
        when(recordRepository.save(any(RecordEntity.class))).thenReturn(recordEntity);

        assertDoesNotThrow(() -> recordImageUseCase.deleteImage(1L));

        verify(recordImageRepository).findById(1L);
        verify(recordImageRepository).deleteById(1L);
        verify(recordRepository).save(any(RecordEntity.class));
    }

    @Test
    void deleteImage_NotFound() {
        when(recordImageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.deleteImage(1L)
        );

        verify(recordImageRepository).findById(1L);
        verify(recordImageRepository, never()).deleteById(any());
    }

    @Test
    void deleteAllImagesForRecord_Success() {
        assertDoesNotThrow(() -> recordImageUseCase.deleteAllImagesForRecord(1L));
        verify(recordImageRepository).deleteByRecordId(1L);
    }
    @Test
    void uploadMultipleImages_Success() {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
                new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test-image-1".getBytes()),
                new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test-image-2".getBytes())
        );

        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        when(recordImageRepository.findByRecordId(1L)).thenReturn(Collections.emptyList());
        when(recordImageRepository.save(any(RecordImageEntity.class))).thenReturn(imageEntity);
        when(recordRepository.save(any(RecordEntity.class))).thenReturn(recordEntity);

        // Act
        List<RecordImageEntity> result = recordImageUseCase.uploadMultipleImages(1L, files);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(recordRepository).findById(1L);
        verify(recordImageRepository).findByRecordId(1L);
        verify(recordImageRepository, times(2)).save(any(RecordImageEntity.class));
        verify(recordRepository).save(recordEntity);
    }

    @Test
    void uploadMultipleImages_RecordNotFound() {
        // Arrange
        List<MultipartFile> files = Collections.singletonList(multipartFile);
        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadMultipleImages(1L, files)
        );

        verify(recordRepository).findById(1L);
        verify(recordImageRepository, never()).save(any());
    }

    @Test
    void uploadMultipleImages_ExceedsMaxImages() {
        // Arrange
        List<MultipartFile> files = Arrays.asList(
                new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "test-image-1".getBytes()),
                new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "test-image-2".getBytes())
        );

        List<RecordImageEntity> existingImages = new ArrayList<>();
        for (int i = 0; i < 4; i++) { // Assuming MAX_IMAGES is 5
            existingImages.add(imageEntity);
        }

        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));
        when(recordImageRepository.findByRecordId(1L)).thenReturn(existingImages);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadMultipleImages(1L, files)
        );

        verify(recordRepository).findById(1L);
        verify(recordImageRepository).findByRecordId(1L);
        verify(recordImageRepository, never()).save(any());
    }

    @Test
    void validateImage_EmptyFile() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile(
                "empty", "empty.jpg", "image/jpeg", new byte[0]
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadMultipleImages(1L, Collections.singletonList(emptyFile))
        );
    }

    @Test
    void validateImage_FileTooLarge() {
        // Arrange
        byte[] largeContent = new byte[6_000_000]; // Larger than 5MB
        MultipartFile largeFile = new MockMultipartFile(
                "large", "large.jpg", "image/jpeg", largeContent
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadMultipleImages(1L, Collections.singletonList(largeFile))
        );
    }

    @Test
    void validateImage_InvalidContentType() {
        // Arrange
        MultipartFile invalidFile = new MockMultipartFile(
                "invalid", "test.txt", "text/plain", "not-an-image".getBytes()
        );

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                recordImageUseCase.uploadMultipleImages(1L, Collections.singletonList(invalidFile))
        );
    }
}