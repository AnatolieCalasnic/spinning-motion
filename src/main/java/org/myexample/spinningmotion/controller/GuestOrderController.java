package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/guest-orders")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j

public class GuestOrderController {
    private final GuestOrderRepository guestOrderRepository;

    @GetMapping("/{purchaseHistoryId}")
    public ResponseEntity<GuestDetailsEntity> getGuestOrder(@PathVariable Long purchaseHistoryId) {
        GuestDetailsEntity guestOrder = guestOrderRepository.findFirstByPurchaseHistoryId(purchaseHistoryId);

        if (guestOrder == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(guestOrder);
    }
    @PostMapping
    public ResponseEntity<GuestDetailsEntity> createGuestOrder(@RequestBody GuestDetails guestDetails) {
        log.info("Creating guest order: {}", guestDetails);
        try {
            GuestDetailsEntity entity = GuestDetailsEntity.builder()
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

            GuestDetailsEntity savedEntity = guestOrderRepository.save(entity);
            log.info("Successfully created guest order: {}", savedEntity);
            return ResponseEntity.ok(savedEntity);
        } catch (Exception e) {
            log.error("Error creating guest order", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<GuestDetailsEntity>> getGuestOrdersByOrder(@PathVariable Long orderId) {
        // This will get all guest details that share the same purchase time
        List<GuestDetailsEntity> guestOrders = guestOrderRepository.findAllByPurchaseHistoryId(orderId);

        if (guestOrders.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(guestOrders);
    }
}