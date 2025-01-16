package org.myexample.spinningmotion.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RecordImageRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecordImageRepository recordImageRepository;

    private RecordEntity testRecord;
    private byte[] testImageData;

    @BeforeEach
    void setUp() {
        // Create a genre for the record
        GenreEntity genre = GenreEntity.builder()
                .name("Rock")
                .build();
        entityManager.persist(genre);

        // Create a test record
        testRecord = RecordEntity.builder()
                .title("Test Album")
                .artist("Test Artist")
                .genre(genre)
                .price(19.99)
                .year(2023)
                .condition("New")
                .quantity(1)
                .build();
        entityManager.persist(testRecord);

        // Create test image data
        testImageData = "Test Image Data".getBytes();
    }

    private RecordImageEntity createTestImage(RecordEntity vRecord, byte[] imageData) {
        return RecordImageEntity.builder()
                .record(vRecord)
                .imageData(imageData)
                .imageType("image/jpeg")
                .build();
    }

    @Test
    void save_ValidRecordImage_SavesSuccessfully() {
        // Arrange
        RecordImageEntity image = createTestImage(testRecord, testImageData);

        // Act
        RecordImageEntity savedImage = recordImageRepository.save(image);

        // Assert
        assertNotNull(savedImage.getId());
        assertEquals(testRecord.getId(), savedImage.getRecord().getId());
        assertArrayEquals(testImageData, savedImage.getImageData());
        assertEquals("image/jpeg", savedImage.getImageType());
    }

    @Test
    void findByRecordId_ExistingRecord_ReturnsImages() {
        // Arrange
        RecordImageEntity image1 = createTestImage(testRecord, testImageData);
        RecordImageEntity image2 = createTestImage(testRecord, "Another Image".getBytes());
        entityManager.persist(image1);
        entityManager.persist(image2);
        entityManager.flush();

        // Act
        List<RecordImageEntity> images = recordImageRepository.findByRecordId(testRecord.getId());

        // Assert
        assertEquals(2, images.size());
        assertTrue(images.stream().allMatch(img -> img.getRecord().getId().equals(testRecord.getId())));
    }

    @Test
    void findByRecordId_NonExistingRecord_ReturnsEmptyList() {
        // Act
        List<RecordImageEntity> images = recordImageRepository.findByRecordId(999L);

        // Assert
        assertTrue(images.isEmpty());
    }

    @Test
    void deleteByRecordId_ExistingRecord_DeletesAllImages() {
        // Arrange
        RecordImageEntity image1 = createTestImage(testRecord, testImageData);
        RecordImageEntity image2 = createTestImage(testRecord, "Another Image".getBytes());
        entityManager.persist(image1);
        entityManager.persist(image2);
        entityManager.flush();

        // Act
        recordImageRepository.deleteByRecordId(testRecord.getId());
        entityManager.flush();

        // Assert
        List<RecordImageEntity> remainingImages = recordImageRepository.findByRecordId(testRecord.getId());
        assertTrue(remainingImages.isEmpty());
    }

    @Test
    void deleteByRecordId_NonExistingRecord_NoException() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            recordImageRepository.deleteByRecordId(999L);
            entityManager.flush();
        });
    }

    @Test
    void existsByRecordId_ExistingRecordWithImages_ReturnsTrue() {
        // Arrange
        RecordImageEntity image = createTestImage(testRecord, testImageData);
        entityManager.persist(image);
        entityManager.flush();

        // Act
        boolean exists = recordImageRepository.existsByRecordId(testRecord.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByRecordId_ExistingRecordWithoutImages_ReturnsFalse() {
        // Act
        boolean exists = recordImageRepository.existsByRecordId(testRecord.getId());

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByRecordId_NonExistingRecord_ReturnsFalse() {
        // Act
        boolean exists = recordImageRepository.existsByRecordId(999L);

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_MultipleLargeImages_SavesSuccessfully() {
        // Arrange
        byte[] largeImageData = new byte[1024 * 1024]; // 1MB image
        RecordImageEntity image1 = createTestImage(testRecord, largeImageData);
        RecordImageEntity image2 = createTestImage(testRecord, largeImageData);

        // Act & Assert
        assertDoesNotThrow(() -> {
            recordImageRepository.save(image1);
            recordImageRepository.save(image2);
            entityManager.flush();
        });
    }
}