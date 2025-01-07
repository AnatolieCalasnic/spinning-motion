package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.hibernate.sql.results.LoadingLogger.LOGGER;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchFilterController {
    private final SearchUseCase searchUseCase;

    @GetMapping
    public ResponseEntity<List<RecordEntity>> searchRecords(
            @RequestParam String searchTerm) {
            try {
                List<RecordEntity> results = searchUseCase.searchRecords(searchTerm);
                return ResponseEntity.ok(results);
            } catch (Exception e) {
                LOGGER.error("Error searching records: ", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
    }
    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseHistoryEntity>> searchOrders(
            @RequestParam String searchTerm) {
        return ResponseEntity.ok(searchUseCase.searchOrders(searchTerm));
    }
}