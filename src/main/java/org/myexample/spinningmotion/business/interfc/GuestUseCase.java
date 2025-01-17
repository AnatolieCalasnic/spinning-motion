package org.myexample.spinningmotion.business.interfc;

import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;

import java.util.List;

public interface GuestUseCase {
    // Create a new guest order
    GuestDetailsEntity createGuestOrder(GuestDetails guestDetails);

    // Get guest order by purchase history ID
    GuestDetailsEntity getGuestOrderByPurchaseId(Long purchaseHistoryId);

    // Get all guest orders for a specific order ID
    List<GuestDetailsEntity> getGuestOrdersByOrderId(Long orderId);
}
