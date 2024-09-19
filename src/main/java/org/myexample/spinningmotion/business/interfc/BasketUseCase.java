package org.myexample.spinningmotion.business.interfc;


import org.myexample.spinningmotion.domain.basket.*;

public interface BasketUseCase {
    GetBasketResponse getBasket(Long userId);
    void addToBasket(AddToBasketRequest request);
    void removeFromBasket(Long userId, Long recordId);
    void updateBasketItemQuantity(UpdateBasketItemQuantityRequest request);
    void clearBasket(Long userId);
}
