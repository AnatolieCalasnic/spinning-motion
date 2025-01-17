package org.myexample.spinningmotion.business.impl.recordtest;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.mockito.InjectMocks;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.impl.record.RecordUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.InventoryTrackingUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.persistence.GenreRepository;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;


import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class RecordUseCaseImplTest {

    @Mock
    private RecordRepository recordRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private InventoryTrackingUseCase inventoryTrackingUseCase;
    @InjectMocks
    private RecordUseCaseImpl recordUseCase;
    @Mock
    private PurchaseHistoryRepository purchaseHistoryRepository;
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
                .condition("Mint")
                .quantity(10)
                .build();

        recordEntity = RecordEntity.builder()
                .id(1L)
                .title("Greatest Hits")
                .artist("Artist Name")
                .genre(genre)
                .price(19.99)
                .year(2020)
                .condition("Mint")
                .quantity(10)
                .images(new ArrayList<>())
                .build();
        lenient().when(genreRepository.findByName("Pop")).thenReturn(Optional.of(genre));
        lenient().when(genreRepository.findByName("Rock")).thenReturn(Optional.of(rockGenre));
    }

    @Test
    void createRecord_Success() {
        when(recordRepository.existsByTitle(createRecordRequest.getTitle())).thenReturn(false);
        when(genreRepository.findById(createRecordRequest.getGenreId())).thenReturn(Optional.of(genre));
        when(recordRepository.save(any(RecordEntity.class))).thenReturn(recordEntity);

        CreateRecordResponse response = recordUseCase.createRecord(createRecordRequest);

        assertNotNull(response);
        assertEquals(recordEntity.getId(), response.getId());
        assertEquals(createRecordRequest.getTitle(), response.getTitle());
        assertEquals(createRecordRequest.getGenreId(), response.getGenreId());

        verify(recordRepository, times(2)).existsByTitle(createRecordRequest.getTitle());
        verify(recordRepository).save(any(RecordEntity.class));
        verify(inventoryTrackingUseCase).broadcastInventoryUpdate(any());

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

        assertThrows(InvalidInputException.class, () -> recordUseCase.createRecord(createRecordRequest));

        verify(recordRepository).existsByTitle(createRecordRequest.getTitle());
        verify(recordRepository, never()).save(any(RecordEntity.class));
        verify(inventoryTrackingUseCase, never()).broadcastInventoryUpdate(any());
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
        GetRecordRequest request = new GetRecordRequest(1L);

        when(recordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> { recordUseCase.getRecord(request);});

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
                .condition("Mint")
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

        verify(recordRepository, times(2)).findById(1L);
        verify(recordRepository).save(any(RecordEntity.class));
        verify(inventoryTrackingUseCase).broadcastInventoryUpdate(any());
    }

    @Test
    void updateRecord_RecordNotFound() {
        UpdateRecordRequest updateRequest = UpdateRecordRequest.builder()
                .id(1L)
                .title("Updated Title")
                .artist("Updated Artist")
                .genreId(1L)
                .price(29.99)
                .year(2021)
                .condition("Mint")
                .quantity(5)
                .build();
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
        verify(inventoryTrackingUseCase).broadcastInventoryUpdate(any());
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
    @Test
    void getTopThreeArtists_Success() {
        // Arrange
        // Create test records for multiple artists with different sales volumes
        RecordEntity artist1Record1 = createTestRecord(1L, "Artist1", "Album1", 10);
        RecordEntity artist1Record2 = createTestRecord(2L, "Artist1", "Album2", 5);
        RecordEntity artist2Record = createTestRecord(3L, "Artist2", "Album3", 8);
        RecordEntity artist3Record = createTestRecord(4L, "Artist3", "Album4", 6);
        RecordEntity artist4Record = createTestRecord(5L, "Artist4", "Album5", 3);

        List<RecordEntity> allRecords = Arrays.asList(
                artist1Record1, artist1Record2, artist2Record,
                artist3Record, artist4Record
        );

        // Create purchase history entries to establish sales rankings
        List<PurchaseHistoryEntity> purchaseHistories = Arrays.asList(
                // Artist1 total sales: 15 (highest)
                createPurchaseHistory(1L, 10), // Album1
                createPurchaseHistory(2L, 5),  // Album2
                // Artist2 total sales: 8 (second)
                createPurchaseHistory(3L, 8),
                // Artist3 total sales: 6 (third)
                createPurchaseHistory(4L, 6),
                // Artist4 total sales: 3 (not in top 3)
                createPurchaseHistory(5L, 3)
        );

        when(recordRepository.findAll()).thenReturn(allRecords);
        when(purchaseHistoryRepository.findAll()).thenReturn(purchaseHistories);

        // Act
        List<GetRecordResponse> topArtists = recordUseCase.getTopThreeArtists();

        // Assert
        assertNotNull(topArtists);
        assertEquals(3, topArtists.size(), "Should return exactly 3 artists");

        // Verify the order of artists based on sales
        assertEquals("Artist1", topArtists.get(0).getArtist(), "First place should be Artist1");
        assertEquals("Artist2", topArtists.get(1).getArtist(), "Second place should be Artist2");
        assertEquals("Artist3", topArtists.get(2).getArtist(), "Third place should be Artist3");

        // Verify correct record selection for each artist
        assertEquals("Album1", topArtists.get(0).getTitle(),
                "Should select the best-selling album for Artist1");

        verify(recordRepository).findAll();
        verify(purchaseHistoryRepository).findAll();
    }

    @Test
    void getTopThreeArtists_LessThanThreeArtists() {
        // Arrange
        RecordEntity onlyArtistRecord = createTestRecord(1L, "Solo Artist", "Album", 5);
        List<RecordEntity> allRecords = Collections.singletonList(onlyArtistRecord);

        List<PurchaseHistoryEntity> purchaseHistories = Collections.singletonList(
                createPurchaseHistory(1L, 5)
        );

        when(recordRepository.findAll()).thenReturn(allRecords);
        when(purchaseHistoryRepository.findAll()).thenReturn(purchaseHistories);

        // Act
        List<GetRecordResponse> topArtists = recordUseCase.getTopThreeArtists();

        // Assert
        assertNotNull(topArtists);
        assertEquals(1, topArtists.size(), "Should return only one artist when less than three exist");
        assertEquals("Solo Artist", topArtists.get(0).getArtist());
    }

    @Test
    void getRecordsByArtist_Success() {
        // Arrange
        String artistName = "Test-Artist";
        String normalizedArtistName = "Test Artist"; // Note the space instead of hyphen

        List<RecordEntity> artistRecords = Arrays.asList(
                createTestRecord(1L, normalizedArtistName, "Album1", 5),
                createTestRecord(2L, normalizedArtistName, "Album2", 3)
        );

        when(recordRepository.findByArtist(normalizedArtistName)).thenReturn(artistRecords);

        // Act
        List<GetRecordResponse> responses = recordUseCase.getRecordsByArtist(artistName);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size(), "Should return all records for the artist");
        assertTrue(responses.stream()
                        .allMatch(r -> r.getArtist().equals(normalizedArtistName)),
                "All records should be from the requested artist");

        verify(recordRepository).findByArtist(normalizedArtistName);
    }

    @Test
    void getRecordsByArtist_NoRecordsFound() {
        // Arrange
        String artistName = "Nonexistent-Artist";
        when(recordRepository.findByArtist("Nonexistent Artist"))
                .thenReturn(Collections.emptyList());

        // Act
        List<GetRecordResponse> responses = recordUseCase.getRecordsByArtist(artistName);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty(), "Should return empty list when no records found");
        verify(recordRepository).findByArtist("Nonexistent Artist");
    }

    // Helper methods for creating test data
    private RecordEntity createTestRecord(Long id, String artist, String title, int quantity) {
        return RecordEntity.builder()
                .id(id)
                .artist(artist)
                .title(title)
                .quantity(quantity)
                .genre(genre) // Using the genre field from setUp()
                .price(19.99)
                .condition("New")
                .images(new ArrayList<>())
                .build();
    }

    private PurchaseHistoryEntity createPurchaseHistory(Long recordId, int quantity) {
        return PurchaseHistoryEntity.builder()
                .recordId(recordId)
                .quantity(quantity)
                .build();
    }
}