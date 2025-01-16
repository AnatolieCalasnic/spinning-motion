package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.subscriber.SubscribeRequest;
import org.myexample.spinningmotion.domain.subscriber.SubscribeResponse;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;

import java.util.List;

public interface SubscriberUseCase {
    SubscribeResponse subscribe(SubscribeRequest request);
    void notifySubscribersOfNewReleases(List<RecordEntity> newRecords);
    void notifySubscribersOfNewRelease(String title, String artist, double price, String genre);
    }
