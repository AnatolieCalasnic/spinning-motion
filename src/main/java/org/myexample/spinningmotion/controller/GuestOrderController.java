package org.myexample.spinningmotion.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.myexample.spinningmotion.business.exception.InvalidInputException;
import org.myexample.spinningmotion.business.impl.user.GuestUseCaseImpl;
import org.myexample.spinningmotion.business.interfc.GuestUseCase;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;
import org.myexample.spinningmotion.domain.response.ErrorResponse;
import org.myexample.spinningmotion.persistence.GuestOrderRepository;
import org.myexample.spinningmotion.persistence.entity.GuestDetailsEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/guest-orders")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j

public class GuestOrderController {
    private final GuestOrderRepository guestOrderRepository;
    private final GuestUseCase guestUseCase;

    @GetMapping("/{purchaseHistoryId}")
    public ResponseEntity<GuestDetailsEntity> getGuestOrder(@PathVariable Long purchaseHistoryId) {
        try {
            GuestDetailsEntity guestOrder = guestUseCase.getGuestOrderByPurchaseId(purchaseHistoryId);
            return ResponseEntity.ok(guestOrder);
        } catch (InvalidInputException e) {
            log.warn("Guest order not found: {}", e.getMessage());
            return ResponseEntity.notFound()
                    .build();
        }
    }
    @PostMapping
    public ResponseEntity<?> createGuestOrder(@Valid @RequestBody GuestDetails guestDetails) {
        log.info("Creating guest order: {}", guestDetails);
        try {
            GuestDetailsEntity savedEntity = guestUseCase.createGuestOrder(guestDetails);
            return ResponseEntity.ok(savedEntity);
        } catch (InvalidInputException e) {
            log.warn("Invalid guest input: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<GuestDetailsEntity>> getGuestOrdersByOrder(@PathVariable Long orderId) {
        try {
            List<GuestDetailsEntity> guestOrders = guestUseCase.getGuestOrdersByOrderId(orderId);
            return ResponseEntity.ok(guestOrders);
        } catch (InvalidInputException e) {
            log.warn("Guest orders not found: {}", e.getMessage());
            return ResponseEntity.notFound()
                    .build();
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Guest

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<?> handleInvalidInputException(InvalidInputException e) {
        log.warn("Invalid input: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception e) {  // Changed to Object
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("An unexpected error occurred"));
    }
}

