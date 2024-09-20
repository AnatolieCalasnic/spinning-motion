package org.myexample.spinningmotion.controller;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.interfc.RecordUseCase;
import org.myexample.spinningmotion.domain.record.*;
import org.myexample.spinningmotion.business.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordController {
    private final RecordUseCase recordUseCase;

    @PostMapping
    public ResponseEntity<CreateRecordResponse> createRecord(@RequestBody CreateRecordRequest request) {
        CreateRecordResponse response = recordUseCase.createRecord(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetRecordResponse> getRecord(@PathVariable Long id) {
        GetRecordResponse response = recordUseCase.getRecord(new GetRecordRequest(id));
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<GetRecordResponse>> getAllRecords() {
        return ResponseEntity.ok(recordUseCase.getAllRecords());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateRecordResponse> updateRecord(@RequestBody UpdateRecordRequest request, @PathVariable Long id) {
        request.setId(id);
        UpdateRecordResponse response = recordUseCase.updateRecord(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRecord(@PathVariable Long id) {
        recordUseCase.deleteRecord(id);
        return ResponseEntity.ok("Record deleted successfully");
    }
    //------------------------------------------------------------------------------------------------------------------
    // Exception Handlers for Record
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<String> handleRecordNotFound(RecordNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}