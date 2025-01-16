package org.myexample.spinningmotion.business.impl.basket;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.*;
import org.myexample.spinningmotion.business.interfc.BasketUseCase;
import org.myexample.spinningmotion.domain.basket.AddToBasketRequest;
import org.myexample.spinningmotion.domain.basket.BasketItem;
import org.myexample.spinningmotion.domain.basket.GetBasketResponse;
import org.myexample.spinningmotion.domain.basket.UpdateBasketItemQuantityRequest;
import org.myexample.spinningmotion.persistence.BasketRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.BasketEntity;
import org.myexample.spinningmotion.persistence.entity.BasketItemEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class BasketUseCaseImpl implements BasketUseCase {
    private static final String BASKET_NOT_FOUND_MESSAGE = "Basket not found for user: ";

    private final BasketRepository basketRepository;
    private final RecordRepository recordRepository;
    private void validateQuantity(int quantity, String itemTitle) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0 for item: " + itemTitle);
        }
    }

    @Override
    public GetBasketResponse getBasket(Long userId) {
        BasketEntity basket = basketRepository.findByUserId(userId)
                .orElseThrow(() -> new BasketNotFoundException(BASKET_NOT_FOUND_MESSAGE + userId));

        List<BasketItem> items = basket.getItems().stream()
                .map(this::convertToBasketItem)
                .toList();

        return GetBasketResponse.builder()
                .id(basket.getId())
                .userId(basket.getUserId())
                .items(items)
                .build();
    }

    @Override
    public void addToBasket(AddToBasketRequest request) {
        BasketEntity basket = basketRepository.findByUserId(request.getUserId())
                .orElseGet(() -> createNewBasket(request.getUserId()));

        RecordEntity vRecord = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RecordNotFoundException("Record not found with id: " + request.getRecordId()));
        BasketItemEntity existingItem = basket.getItems().stream()
                .filter(item -> item.getRecordId().equals(request.getRecordId()))
                .findFirst()
                .orElse(null);

        // Calculate total requested quantity (existing + new)
        int totalRequestedQuantity = (existingItem != null ? existingItem.getQuantity() : 0) + request.getQuantity();

        validateQuantity(totalRequestedQuantity, vRecord.getTitle());

        // Check if enough stock is available
        if (vRecord.getQuantity() < totalRequestedQuantity) {
            throw new OutOfStockException(vRecord.getTitle(), totalRequestedQuantity, vRecord.getQuantity());
        }

        if (existingItem != null) {
            // Update existing item quantity
            existingItem.setQuantity(totalRequestedQuantity);
        } else {
            // Create new basket item
            BasketItemEntity newItem = new BasketItemEntity();
            newItem.setRecordId(request.getRecordId());
            newItem.setQuantity(request.getQuantity());
            basket.getItems().add(newItem);
        }

        basketRepository.save(basket);

        // updating the record's quantity
        vRecord.setQuantity(vRecord.getQuantity() - request.getQuantity());
        recordRepository.save(vRecord);
    }

    @Override
    public void updateBasketItemQuantity(UpdateBasketItemQuantityRequest request) {
        validateQuantity(request.getQuantity(), "Record");

        BasketEntity basket = basketRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new BasketNotFoundException(BASKET_NOT_FOUND_MESSAGE  + request.getUserId()));

        BasketItemEntity itemToUpdate = basket.getItems().stream()
                .filter(item -> item.getRecordId().equals(request.getRecordId()))
                .findFirst()
                .orElseThrow(() -> new RecordNotInBasketException(request.getRecordId(), request.getUserId()));

        RecordEntity vRecord = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RecordNotFoundException(BASKET_NOT_FOUND_MESSAGE  + request.getRecordId()));

        if (vRecord.getQuantity() < request.getQuantity()) {
            throw new OutOfStockException(vRecord.getTitle(), request.getQuantity(), vRecord.getQuantity());
        }

        itemToUpdate.setQuantity(request.getQuantity());
        basketRepository.save(basket);
    }

    @Override
    public void removeFromBasket(Long userId, Long recordId) {
        BasketEntity basket = basketRepository.findByUserId(userId)
                .orElseThrow(() -> new BasketNotFoundException(BASKET_NOT_FOUND_MESSAGE  + userId));

        boolean removed = basket.getItems().removeIf(item -> item.getRecordId().equals(recordId));
        if (!removed) {
            throw new RecordNotInBasketException(recordId, userId);
        }

        basketRepository.save(basket);
    }

    @Override
    public void clearBasket(Long userId) {
        BasketEntity basket = basketRepository.findByUserId(userId)
                .orElseGet(() -> {
                    BasketEntity newBasket = createNewBasket(userId);
                    return basketRepository.save(newBasket);
                });

        basket.getItems().clear();
        basketRepository.save(basket);
    }

    private BasketEntity createNewBasket(Long userId) {
        return BasketEntity.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .build();
    }

    private BasketItem convertToBasketItem(BasketItemEntity entity) {
        return BasketItem.builder()
                .recordId(entity.getRecordId())
                .quantity(entity.getQuantity())
                .build();
    }
}
