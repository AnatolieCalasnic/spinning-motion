package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.hibernate.AnnotationException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.myexample.spinningmotion.business.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase-history")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class PurchaseHistoryController {

    private final PurchaseHistoryUseCase purchaseHistoryUseCase;

    @PostMapping
    public ResponseEntity<CreatePurchaseHistoryResponse> createPurchaseHistory(@RequestBody CreatePurchaseHistoryRequest request) {
        CreatePurchaseHistoryResponse response = purchaseHistoryUseCase.createPurchaseHistory(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<GetPurchaseHistoryResponse>> getAllPurchaseHistories(@PathVariable Long userId) {
        return ResponseEntity.ok(purchaseHistoryUseCase.getAllPurchaseHistories(userId));
    }

    @GetMapping("/history/{id}")
    public ResponseEntity<GetPurchaseHistoryResponse> getPurchaseHistory(@PathVariable Long id) {
        GetPurchaseHistoryResponse response = purchaseHistoryUseCase.getPurchaseHistory(new GetPurchaseHistoryRequest(id));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePurchaseHistory(@PathVariable Long id) {
        purchaseHistoryUseCase.deletePurchaseHistory(id);
        return ResponseEntity.ok("Purchase history deleted");
    }

    @GetMapping("/admin/dashboard")
    public ResponseEntity<AdminDashboardStats> getAdminDashboardStats() {
        return ResponseEntity.ok(purchaseHistoryUseCase.getAdminDashboardStats());
    }

    @GetMapping("/stats")
    public ResponseEntity<PurchaseHistoryStats> getPurchaseHistoryStats() {
        return ResponseEntity.ok(purchaseHistoryUseCase.getPurchaseHistoryStats());
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GetPurchaseHistoryResponse>> getRecentPurchaseHistories() {
        return ResponseEntity.ok(purchaseHistoryUseCase.getRecentPurchaseHistories(10));
    }
    @GetMapping("/all")
    public ResponseEntity<List<GetPurchaseHistoryResponse>> getAllPurchaseHistories() {
        return ResponseEntity.ok(purchaseHistoryUseCase.getAllPurchaseHistories());
    }
    @GetMapping("/related/{id}")
    public ResponseEntity<List<GetPurchaseHistoryResponse>> getRelatedOrders(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseHistoryUseCase.getRelatedOrders(id));
    }
    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Purchase History
    @ExceptionHandler(PurchaseHistoryNotFoundException.class)
    public ResponseEntity<String> handlePurchaseHistoryNotFound(PurchaseHistoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(AnnotationException.class)
    public ResponseEntity<String> handleAnnotationException(AnnotationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error in entity mapping: " + ex.getMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
    @ExceptionHandler(InsufficientQuantityException.class)
    public ResponseEntity<String> handleInsufficientQuantity(InsufficientQuantityException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}