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
@CrossOrigin(origins = "http://localhost:3000")
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
    @GetMapping("/genre/{genreName}")
    public ResponseEntity<List<GetRecordResponse>> getRecordsByGenre(@PathVariable String genreName) {
        List<GetRecordResponse> response = recordUseCase.getRecordsByGenre(genreName);
        return ResponseEntity.ok(response);
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
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
}