package org.myexample.spinningmotion.persistence;


import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.RecordImageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RecordRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RecordRepository recordRepository;

    private GenreEntity createTestGenre() {
        return GenreEntity.builder()
                .name("Rock")
                .build();
    }

    private RecordEntity createTestRecord(GenreEntity genre) {
        return RecordEntity.builder()
                .title("Test Album")
                .artist("Test Artist")
                .genre(genre)
                .price(29.99)
                .year(2023)
                .condition("New")
                .quantity(10)
                .images(new ArrayList<>())
                .build();
    }

    private RecordImageEntity createTestImage(RecordEntity vRecord) {
        return RecordImageEntity.builder()
                .record(vRecord)
                .imageType("jpg")
                .imageData("test-data".getBytes())
                .build();
    }

    @Test
    void save_ValidRecord_SavesSuccessfully() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);

        // Act
        RecordEntity savedRecord = recordRepository.save(vRecord);

        // Assert
        assertNotNull(savedRecord.getId());
        assertEquals("Test Album", savedRecord.getTitle());
    }

    @Test
    void findById_ExistingRecord_ReturnsRecord() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);
        entityManager.persist(vRecord);
        entityManager.flush();

        // Act
        Optional<RecordEntity> foundRecord = recordRepository.findById(vRecord.getId());

        // Assert
        assertTrue(foundRecord.isPresent());
        assertEquals("Test Album", foundRecord.get().getTitle());
    }

    @Test
    void findAll_MultipleRecords_ReturnsAllRecords() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        entityManager.persist(createTestRecord(genre));
        entityManager.persist(createTestRecord(genre));
        entityManager.flush();

        // Act
        List<RecordEntity> records = recordRepository.findAll();

        // Assert
        assertEquals(2, records.size());
    }

    @Test
    void existsByTitle_ExistingTitle_ReturnsTrue() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);
        entityManager.persist(vRecord);
        entityManager.flush();

        // Act
        boolean exists = recordRepository.existsByTitle("Test Album");

        // Assert
        assertTrue(exists);
    }

    @Test
    void saveWithImage_ValidRecord_SavesWithImage() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);
        RecordImageEntity image = createTestImage(vRecord);
        vRecord.setImages(List.of(image));

        // Act
        RecordEntity savedRecord = recordRepository.save(vRecord);

        // Assert
        assertNotNull(savedRecord.getId());
        assertEquals(1, savedRecord.getImages().size());
    }
    @Test
    void update_RecordQuantity_UpdatesSuccessfully() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);
        entityManager.persist(vRecord);
        entityManager.flush();

        // Act
        vRecord.setQuantity(20);
        RecordEntity updatedRecord = recordRepository.save(vRecord);

        // Assert
        assertEquals(20, updatedRecord.getQuantity());
    }

    @Test
    void save_RecordWithMultipleImages_SavesAllImages() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);
        vRecord.setImages(List.of(
                createTestImage(vRecord),
                createTestImage(vRecord)
        ));

        // Act
        RecordEntity savedRecord = recordRepository.save(vRecord);

        // Assert
        assertEquals(2, savedRecord.getImages().size());
    }

    @Test
    void findAll_WithPagination_ReturnsCorrectPage() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        for(int i = 0; i < 5; i++) {
            entityManager.persist(createTestRecord(genre));
        }
        entityManager.flush();

        // Act
        Page<RecordEntity> recordPage = recordRepository.findAll(PageRequest.of(0, 3));

        // Assert
        assertEquals(3, recordPage.getContent().size());
        assertEquals(5, recordPage.getTotalElements());
    }

    @Test
    void update_RecordPrice_UpdatesSuccessfully() {
        // Arrange
        GenreEntity genre = createTestGenre();
        entityManager.persist(genre);

        RecordEntity vRecord = createTestRecord(genre);
        entityManager.persist(vRecord);
        entityManager.flush();

        // Act
        vRecord.setPrice(39.99);
        RecordEntity updatedRecord = recordRepository.save(vRecord);

        // Assert
        assertEquals(39.99, updatedRecord.getPrice());
    }
}