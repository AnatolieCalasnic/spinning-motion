package org.myexample.spinningmotion.business.impl.subscriber;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.EmailUseCase;
import org.myexample.spinningmotion.business.interfc.SubscriberUseCase;
import org.myexample.spinningmotion.domain.subscriber.SubscribeRequest;
import org.myexample.spinningmotion.domain.subscriber.SubscribeResponse;
import org.myexample.spinningmotion.persistence.SubscriberRepository;
import org.myexample.spinningmotion.persistence.entity.GenreEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.myexample.spinningmotion.persistence.entity.SubscriberEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriberUseCaseImpl implements SubscriberUseCase {
    private final SubscriberRepository subscriberRepository;
    private final EmailUseCase emailUseCase;

    @Override
    public SubscribeResponse subscribe(SubscribeRequest request) {
        if (subscriberRepository.existsByEmail(request.getEmail())) {
            SubscriberEntity existingSubscriber = subscriberRepository.findByEmail(request.getEmail())
                    .orElseThrow();
            return toResponse(existingSubscriber);
        }

        SubscriberEntity subscriber = toEntity(request);
        SubscriberEntity savedSubscriber = subscriberRepository.save(subscriber);
        return toResponse(savedSubscriber);
    }

    @Override
    public void notifySubscribersOfNewReleases(List<RecordEntity> newRecords) {
        if (newRecords == null || newRecords.isEmpty()) {
            return;
        }
        List<SubscriberEntity> subscribers = subscriberRepository.findAll();
        for (SubscriberEntity subscriber : subscribers) {
            emailUseCase.sendNewReleaseNotification(subscriber.getEmail(), newRecords);
        }
    }
    @Override
    public void notifySubscribersOfNewRelease(String title, String artist, double price, String genre) {
        RecordEntity newRecord = RecordEntity.builder()
                .title(title)
                .artist(artist)
                .price(price)
                .genre(GenreEntity.builder().name(genre).build())
                .build();

        notifySubscribersOfNewReleases(List.of(newRecord));
    }
    private SubscriberEntity toEntity(SubscribeRequest request) {
        return SubscriberEntity.builder()
                .email(request.getEmail())
                .build();
    }

    private SubscribeResponse toResponse(SubscriberEntity entity) {
        return SubscribeResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .build();
    }
}
