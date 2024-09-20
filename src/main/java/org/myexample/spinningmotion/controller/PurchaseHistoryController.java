package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
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
    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Purchase History
    @ExceptionHandler(PurchaseHistoryNotFoundException.class)
    public ResponseEntity<String> handlePurchaseHistoryNotFound(PurchaseHistoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}