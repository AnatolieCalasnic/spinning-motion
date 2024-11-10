package org.myexample.spinningmotion.persistence;

import org.junit.jupiter.api.Test;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GenreRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GenreRepository genreRepository;

    private GenreEntity createTestGenre(String name) {
        return GenreEntity.builder()
                .name(name)
                .build();
    }

    @Test
    void save_ValidGenre_SavesSuccessfully() {
        // Arrange
        GenreEntity genre = createTestGenre("Rock");

        // Act
        GenreEntity savedGenre = genreRepository.save(genre);

        // Assert
        assertNotNull(savedGenre.getId());
        assertEquals("Rock", savedGenre.getName());
    }

    @Test
    void findByName_ExistingName_ReturnsGenre() {
        // Arrange
        GenreEntity genre = createTestGenre("Jazz");
        entityManager.persist(genre);
        entityManager.flush();

        // Act
        Optional<GenreEntity> foundGenre = genreRepository.findByName("Jazz");

        // Assert
        assertTrue(foundGenre.isPresent());
        assertEquals("Jazz", foundGenre.get().getName());
    }

    @Test
    void findByName_NonExistingName_ReturnsEmpty() {
        // Act
        Optional<GenreEntity> foundGenre = genreRepository.findByName("NonExistent");

        // Assert
        assertTrue(foundGenre.isEmpty());
    }

    @Test
    void findAll_ReturnsAllGenres() {
        // Arrange
        entityManager.persist(createTestGenre("Rock"));
        entityManager.persist(createTestGenre("Jazz"));
        entityManager.flush();

        // Act
        List<GenreEntity> genres = genreRepository.findAll();

        // Assert
        assertEquals(2, genres.size());
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Rock")));
        assertTrue(genres.stream().anyMatch(genre -> genre.getName().equals("Jazz")));
    }

    @Test
    void deleteById_ExistingGenre_DeletesSuccessfully() {
        // Arrange
        GenreEntity genre = createTestGenre("Pop");
        entityManager.persist(genre);
        entityManager.flush();
        Long genreId = genre.getId();

        // Act
        genreRepository.deleteById(genreId);
        entityManager.flush();

        // Assert
        assertNull(entityManager.find(GenreEntity.class, genreId));
    }

    @Test
    void existsByName_ExistingName_ReturnsTrue() {
        // Arrange
        entityManager.persist(createTestGenre("Classical"));
        entityManager.flush();

        // Act
        boolean exists = genreRepository.existsByName("Classical");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_NonExistingName_ReturnsFalse() {
        // Act
        boolean exists = genreRepository.existsByName("NonExistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsAny_WithGenres_ReturnsTrue() {
        // Arrange
        entityManager.persist(createTestGenre("Rock"));
        entityManager.flush();

        // Act & Assert
        assertTrue(genreRepository.existsAny());
    }

    @Test
    void existsAny_NoGenres_ReturnsFalse() {
        // Act & Assert
        assertFalse(genreRepository.existsAny());
    }
}