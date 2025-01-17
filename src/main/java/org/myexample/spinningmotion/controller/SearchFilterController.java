package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.SearchUseCase;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;


@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchFilterController {
    private static final Logger logger = LoggerFactory.getLogger(SearchFilterController.class);

    private final SearchUseCase searchUseCase;

    @GetMapping
    public ResponseEntity<List<RecordEntity>> searchRecords(
            @RequestParam(required = false) String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }
            List<RecordEntity> results = searchUseCase.searchRecords(searchTerm);
           results.forEach(record -> {
                if (record.getImages() != null) {
                    record.getImages().forEach(image -> image.setRecord(null));
                }
            });
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error searching records: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseHistoryEntity>> searchOrders(
            @RequestParam String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<PurchaseHistoryEntity> results = searchUseCase.searchOrders(searchTerm);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Error searching orders: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}