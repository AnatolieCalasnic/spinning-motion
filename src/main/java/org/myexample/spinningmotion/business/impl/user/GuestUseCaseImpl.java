package org.myexample.spinningmotion.business.impl.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.interfc.GuestUseCase;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuestUseCaseImpl implements GuestUseCase {
    private final GuestOrderRepository guestOrderRepository;
    private final PurchaseHistoryRepository purchaseHistoryRepository;
    @Override
    @Transactional
    public GuestDetailsEntity createGuestOrder(GuestDetails guestDetails) {
        log.info("Creating guest order: {}", guestDetails);
        validateGuestDetails(guestDetails);

        //Validating purchase id
        if (purchaseHistoryRepository.findById(guestDetails.getPurchaseHistoryId()).isEmpty()) {
            throw new InvalidInputException("Invalid purchase history ID");
        }

        try {
            GuestDetailsEntity entity = convertToEntity(guestDetails);
            GuestDetailsEntity savedEntity = guestOrderRepository.save(entity);
            log.info("Successfully created guest order: {}", savedEntity);
            return savedEntity;
        } catch (Exception e) {
            log.error("Failed to create guest order", e);
            throw new InvalidInputException("Failed to create guest order: " + e.getMessage());
        }
    }

    @Override
    public GuestDetailsEntity getGuestOrderByPurchaseId(Long purchaseHistoryId) {
        GuestDetailsEntity guestOrder = guestOrderRepository.findFirstByPurchaseHistoryId(purchaseHistoryId);
        if (guestOrder == null) {
            throw new InvalidInputException("Guest order not found for purchase history ID: " + purchaseHistoryId);
        }
        return guestOrder;
    }

    @Override
    public List<GuestDetailsEntity> getGuestOrdersByOrderId(Long orderId) {
        List<GuestDetailsEntity> guestOrders = guestOrderRepository.findAllByPurchaseHistoryId(orderId);
        if (guestOrders.isEmpty()) {
            throw new InvalidInputException("No guest orders found for order ID: " + orderId);
        }
        return guestOrders;
    }

    private void validateGuestDetails(GuestDetails guestDetails) {
        validateNames(guestDetails);
        validateContactInfo(guestDetails);
        validateAddress(guestDetails);
    }

    private void validateNames(GuestDetails guestDetails) {
        if (guestDetails.getFname() == null || !guestDetails.getFname().matches("^[\\p{L}\\s]{2,50}$")) {
            throw new InvalidInputException("First name must be between 2 and 50 characters and contain only letters");
        }

        if (guestDetails.getLname() == null || !guestDetails.getLname().matches("^[\\p{L}\\s]{2,50}$")) {
            throw new InvalidInputException("Last name must be between 2 and 50 characters and contain only letters");
        }
    }

    private void validateContactInfo(GuestDetails guestDetails) {
        if (guestDetails.getEmail() == null || !guestDetails.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new InvalidInputException("Invalid email format");
        }

        if (guestDetails.getPhonenum() == null || !guestDetails.getPhonenum().matches("^\\+?[\\d\\s-]{6,15}$")) {
            throw new InvalidInputException("Invalid phone number format");
        }
    }

    private void validateAddress(GuestDetails guestDetails) {
        validateRequiredField(guestDetails.getAddress(), "Address");
        validateRequiredField(guestDetails.getCity(), "City");
        validateRequiredField(guestDetails.getRegion(), "Region");
        validateRequiredField(guestDetails.getCountry(), "Country");

        if (guestDetails.getPostalCode() == null || !guestDetails.getPostalCode().matches("^[A-Z0-9\\s-]{3,10}$")) {
            throw new InvalidInputException("Invalid postal code format");
        }
    }

    private void validateRequiredField(String field, String fieldName) {
        if (field == null || field.trim().isEmpty()) {
            throw new InvalidInputException(fieldName + " is required");
        }
    }

    private GuestDetailsEntity convertToEntity(GuestDetails guestDetails) {
        return GuestDetailsEntity.builder()
                .purchaseHistoryId(guestDetails.getPurchaseHistoryId())
                .fname(guestDetails.getFname())
                .lname(guestDetails.getLname())
                .email(guestDetails.getEmail())
                .address(guestDetails.getAddress())
                .postalCode(guestDetails.getPostalCode())
                .country(guestDetails.getCountry())
                .city(guestDetails.getCity())
                .region(guestDetails.getRegion())
                .phonenum(guestDetails.getPhonenum())
                .build();
    }
}