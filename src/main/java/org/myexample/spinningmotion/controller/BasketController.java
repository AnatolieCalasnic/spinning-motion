package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.BasketUseCase;
import org.myexample.spinningmotion.domain.basket.*;
import org.myexample.spinningmotion.business.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/basket")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class BasketController {

    private final BasketUseCase basketUseCase;

    @GetMapping("/{userId}")
    public ResponseEntity<GetBasketResponse> getBasket(@PathVariable Long userId) {
        GetBasketResponse response = basketUseCase.getBasket(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addToBasket(@RequestBody AddToBasketRequest request) {
        basketUseCase.addToBasket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Item added to basket");
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateBasketItemQuantity(@RequestBody UpdateBasketItemQuantityRequest request) {
        basketUseCase.updateBasketItemQuantity(request);
        return ResponseEntity.ok("Basket item quantity updated");
    }

    @DeleteMapping("/{userId}/{recordId}")
    public ResponseEntity<String> removeFromBasket(@PathVariable Long userId, @PathVariable Long recordId) {
        basketUseCase.removeFromBasket(userId, recordId);
        return ResponseEntity.ok("Item removed from basket");
    }

    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<String> clearBasket(@PathVariable Long userId) {
        basketUseCase.clearBasket(userId);
        return ResponseEntity.ok("Basket cleared");
    }

    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Basket
    @ExceptionHandler(BasketNotFoundException.class)
    public ResponseEntity<String> handleBasketNotFound(BasketNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(RecordNotInBasketException.class)
    public ResponseEntity<String> handleRecordNotInBasket(RecordNotInBasketException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<String> handleOutOfStock(OutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}