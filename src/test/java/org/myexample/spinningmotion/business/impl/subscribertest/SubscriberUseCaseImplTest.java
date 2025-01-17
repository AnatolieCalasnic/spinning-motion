package org.myexample.spinningmotion.business.impl.subscribertest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.myexample.spinningmotion.business.impl.subscriber.SubscriberUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.EmailUseCase;
import org.myexample.spinningmotion.domain.subscriber.SubscribeRequest;
import org.myexample.spinningmotion.domain.subscriber.SubscribeResponse;
import org.myexample.spinningmotion.persistence.SubscriberRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.SubscriberEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriberUseCaseImplTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private EmailUseCase emailUseCase;

    @InjectMocks
    private SubscriberUseCaseImpl subscriberUseCase;

    private SubscriberEntity testSubscriberEntity;
    private SubscribeRequest testSubscribeRequest;
    private List<RecordEntity> testNewRecords;
    private static final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        // Setup test subscriber entity
        testSubscriberEntity = SubscriberEntity.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .build();

        // Setup test subscribe request
        testSubscribeRequest = SubscribeRequest.builder()
                .email(TEST_EMAIL)
                .build();

        // Setup test records for notifications
        testNewRecords = Arrays.asList(
                RecordEntity.builder()
                        .id(1L)
                        .title("Test Album")
                        .artist("Test Artist")
                        .price(29.99)
                        .genre(GenreEntity.builder().name("Rock").build())
                        .build()
        );
    }

    @Test
    void subscribe_NewSubscriber_Success() {
        // Given
        when(subscriberRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(subscriberRepository.save(any(SubscriberEntity.class))).thenReturn(testSubscriberEntity);

        // When
        SubscribeResponse response = subscriberUseCase.subscribe(testSubscribeRequest);

        // Then
        assertEquals(testSubscriberEntity.getId(), response.getId());
        assertEquals(testSubscriberEntity.getEmail(), response.getEmail());
        verify(subscriberRepository).existsByEmail(TEST_EMAIL);
        verify(subscriberRepository).save(any(SubscriberEntity.class));
    }

    @Test
    void subscribe_ExistingSubscriber_ReturnsExistingSubscription() {
        // Given
        when(subscriberRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);
        when(subscriberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testSubscriberEntity));

        // When
        SubscribeResponse response = subscriberUseCase.subscribe(testSubscribeRequest);

        // Then
        assertEquals(testSubscriberEntity.getId(), response.getId());
        assertEquals(testSubscriberEntity.getEmail(), response.getEmail());
        verify(subscriberRepository).existsByEmail(TEST_EMAIL);
        verify(subscriberRepository).findByEmail(TEST_EMAIL);
        verify(subscriberRepository, never()).save(any(SubscriberEntity.class));
    }

    @Test
    void notifySubscribersOfNewReleases_MultipleSubscribers() {
        // Given
        List<SubscriberEntity> subscribers = Arrays.asList(
                SubscriberEntity.builder().email("subscriber1@example.com").build(),
                SubscriberEntity.builder().email("subscriber2@example.com").build()
        );
        when(subscriberRepository.findAll()).thenReturn(subscribers);

        // When
        subscriberUseCase.notifySubscribersOfNewReleases(testNewRecords);

        // Then
        verify(subscriberRepository).findAll();
        verify(emailUseCase, times(2))
                .sendNewReleaseNotification(anyString(), eq(testNewRecords));
        verify(emailUseCase).sendNewReleaseNotification("subscriber1@example.com", testNewRecords);
        verify(emailUseCase).sendNewReleaseNotification("subscriber2@example.com", testNewRecords);
    }

    @Test
    void notifySubscribersOfNewReleases_NoSubscribers() {
        // Given
        when(subscriberRepository.findAll()).thenReturn(List.of());

        // When
        subscriberUseCase.notifySubscribersOfNewReleases(testNewRecords);

        // Then
        verify(subscriberRepository).findAll();
        verifyNoInteractions(emailUseCase);
    }

    @Test
    void notifySubscribersOfNewRelease_SingleRecord_MultipleSubscribers() {
        // Given
        List<SubscriberEntity> subscribers = Arrays.asList(
                SubscriberEntity.builder().email("subscriber1@example.com").build(),
                SubscriberEntity.builder().email("subscriber2@example.com").build()
        );
        when(subscriberRepository.findAll()).thenReturn(subscribers);

        String title = "New Album";
        String artist = "New Artist";
        double price = 19.99;
        String genre = "Jazz";

        // When
        subscriberUseCase.notifySubscribersOfNewRelease(title, artist, price, genre);

        // Then
        verify(subscriberRepository).findAll();

        verify(emailUseCase, times(2)).sendNewReleaseNotification(anyString(), argThat(records ->
                records.size() == 1 &&
                        records.get(0).getTitle().equals(title) &&
                        records.get(0).getArtist().equals(artist) &&
                        records.get(0).getPrice() == price &&
                        records.get(0).getGenre().getName().equals(genre)
        ));

        // Verify specific emailtest addresses were notified
        verify(emailUseCase).sendNewReleaseNotification(eq("subscriber1@example.com"), any());
        verify(emailUseCase).sendNewReleaseNotification(eq("subscriber2@example.com"), any());
    }

    @Test
    void notifySubscribersOfNewRelease_SingleRecord_NoSubscribers() {
        // Given
        when(subscriberRepository.findAll()).thenReturn(List.of());

        // When
        subscriberUseCase.notifySubscribersOfNewRelease(
                "New Album", "New Artist", 19.99, "Jazz");

        // Then
        verify(subscriberRepository).findAll();
        verifyNoInteractions(emailUseCase);
    }
}