package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchFilterController {
    private final SearchUseCase searchUseCase;

    @GetMapping
    public ResponseEntity<List<RecordEntity>> searchRecords(
            @RequestParam String searchTerm) {
        return ResponseEntity.ok(searchUseCase.searchRecords(searchTerm));
    }
    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseHistoryEntity>> searchOrders(
            @RequestParam String searchTerm) {
        return ResponseEntity.ok(searchUseCase.searchOrders(searchTerm));
    }
}