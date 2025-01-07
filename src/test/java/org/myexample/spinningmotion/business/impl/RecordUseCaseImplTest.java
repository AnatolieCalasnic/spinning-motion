package org.myexample.spinningmotion.business.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.impl.record.RecordUseCaseImpl;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;


import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class RecordUseCaseImplTest {

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private GenreRepository genreRepository;
    @InjectMocks
    private RecordUseCaseImpl recordUseCase;

    private CreateRecordRequest createRecordRequest;
    private RecordEntity recordEntity;
    private GenreEntity genre;
    private GenreEntity rockGenre;

    @BeforeEach
    void setUp() {
        genre = GenreEntity.builder()
                .id(1L)
                .name("Pop")
                .build();
        rockGenre = GenreEntity.builder()
                .id(2L)
                .name("Rock")
                .build();
        createRecordRequest = CreateRecordRequest.builder()
                .title("Greatest Hits")
                .artist("Artist Name")
                .genreId(genre.getId())
                .price(19.99)
                .year(2020)
                .condition("New")
                .quantity(10)
                .build();

        recordEntity = RecordEntity.builder()
                .id(1L)
                .title("Greatest Hits")
                .artist("Artist Name")
                .genre(genre)
                .price(19.99)
                .year(2020)
                .condition("New")
                .quantity(10)
                .images(new ArrayList<>())
                .build();
        lenient().when(genreRepository.findByName("Pop")).thenReturn(Optional.of(genre));
        lenient().when(genreRepository.findByName("Rock")).thenReturn(Optional.of(rockGenre));
    }

    @Test
    void createRecord_Success() {
        when(genreRepository.findById(createRecordRequest.getGenreId())).thenReturn(Optional.of(genre));
        when(recordRepository.existsByTitle(createRecordRequest.getTitle())).thenReturn(false);
        when(recordRepository.save(any(RecordEntity.class))).thenReturn(recordEntity);


        CreateRecordResponse response = recordUseCase.createRecord(createRecordRequest);

        assertNotNull(response);
        assertEquals(recordEntity.getId(), response.getId());
        assertEquals(createRecordRequest.getTitle(), response.getTitle());
        assertEquals(createRecordRequest.getGenreId(), response.getGenreId());


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
        assertNotNull(response.getImages());
        assertTrue(response.getImages().isEmpty());
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
        when(recordRepository.findAllWithImages()).thenReturn(recordEntities);

        List<GetRecordResponse> responses = recordUseCase.getAllRecords();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(recordEntity.getId(), responses.get(0).getId());
        assertEquals(recordEntity.getTitle(), responses.get(0).getTitle());

        verify(recordRepository).findAllWithImages();
    }
    @Test
    void getRecordsByGenre_Success() {
        // Arrange
        List<RecordEntity> recordEntities = Arrays.asList(recordEntity);
        when(recordRepository.findByGenreName("pop")).thenReturn(recordEntities);

        // Act
        List<GetRecordResponse> responses = recordUseCase.getRecordsByGenre("Pop");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(recordEntity.getId(), responses.get(0).getId());
        assertEquals(recordEntity.getTitle(), responses.get(0).getTitle());
        assertEquals(recordEntity.getArtist(), responses.get(0).getArtist());
        assertEquals(recordEntity.getPrice(), responses.get(0).getPrice());
        assertEquals(recordEntity.getYear(), responses.get(0).getYear());
        assertEquals(recordEntity.getCondition(), responses.get(0).getCondition());
        assertEquals(recordEntity.getQuantity(), responses.get(0).getQuantity());

        verify(recordRepository).findByGenreName("pop");
    }

    @Test
    void getRecordsByGenre_EmptyList() {
        // Arrange
        when(recordRepository.findByGenreName("pop")).thenReturn(Collections.emptyList());

        // Act
        List<GetRecordResponse> responses = recordUseCase.getRecordsByGenre("Pop");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(recordRepository).findByGenreName("pop");
    }
    @Test
    void updateRecord_Success() {
        UpdateRecordRequest updateRequest = UpdateRecordRequest.builder()
                .id(1L)
                .title("New Title")
                .artist("New Artist")
                .genreId(1L)
                .price(15.99)
                .year(2021)
                .condition("Used")
                .quantity(5)
                .build();

        when(recordRepository.findById(1L)).thenReturn(Optional.of(recordEntity));

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        when(recordRepository.save(any(RecordEntity.class))).thenReturn(
                recordEntity.toBuilder()
                        .title(updateRequest.getTitle())
                        .artist(updateRequest.getArtist())
                        .genre(genre)
                        .price(updateRequest.getPrice())
                        .year(updateRequest.getYear())
                        .condition(updateRequest.getCondition())
                        .quantity(updateRequest.getQuantity())
                        .build()
        );

        UpdateRecordResponse response = recordUseCase.updateRecord(updateRequest);

        assertNotNull(response);
        assertEquals(recordEntity.getId(), response.getId());
        assertEquals(updateRequest.getTitle(), response.getTitle());
        assertEquals(updateRequest.getArtist(), response.getArtist());
        assertEquals(updateRequest.getPrice(), response.getPrice());
        assertEquals(updateRequest.getCondition(), response.getCondition());
        assertEquals(updateRequest.getYear(), response.getYear());
        assertEquals(updateRequest.getQuantity(), response.getQuantity());

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
    @Test
    void getNewReleases_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<RecordEntity> newReleases = Arrays.asList(recordEntity);
        when(recordRepository.findNewReleases(any(LocalDateTime.class))).thenReturn(newReleases);

        // Act
        List<GetRecordResponse> responses = recordUseCase.getNewReleases(startDate);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(recordEntity.getId(), responses.get(0).getId());
        assertEquals(recordEntity.getTitle(), responses.get(0).getTitle());
        assertEquals(recordEntity.getArtist(), responses.get(0).getArtist());

        verify(recordRepository).findNewReleases(startDate);
    }

    @Test
    void getNewReleases_EmptyList() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        when(recordRepository.findNewReleases(any(LocalDateTime.class))).thenReturn(Collections.emptyList());

        // Act
        List<GetRecordResponse> responses = recordUseCase.getNewReleases(startDate);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(recordRepository).findNewReleases(startDate);
    }

    @Test
    void getNewReleasesByGenre_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        List<RecordEntity> newReleases = Arrays.asList(recordEntity);
        when(recordRepository.findNewReleasesByGenre(any(LocalDateTime.class), eq("pop"))).thenReturn(newReleases);

        // Act
        List<GetRecordResponse> responses = recordUseCase.getNewReleasesByGenre(startDate, "Pop");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(recordEntity.getId(), responses.get(0).getId());
        assertEquals(recordEntity.getTitle(), responses.get(0).getTitle());
        assertEquals(recordEntity.getArtist(), responses.get(0).getArtist());

        verify(recordRepository).findNewReleasesByGenre(startDate, "pop");
    }

    @Test
    void getNewReleasesByGenre_EmptyList() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        when(recordRepository.findNewReleasesByGenre(any(LocalDateTime.class), eq("pop"))).thenReturn(Collections.emptyList());

        // Act
        List<GetRecordResponse> responses = recordUseCase.getNewReleasesByGenre(startDate, "Pop");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(recordRepository).findNewReleasesByGenre(startDate, "pop");
    }
}