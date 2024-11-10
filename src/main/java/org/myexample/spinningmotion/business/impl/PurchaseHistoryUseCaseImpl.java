package org.myexample.spinningmotion.business.impl;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryUseCaseImpl implements PurchaseHistoryUseCase {
    private final PurchaseHistoryRepository purchaseHistoryRepository;

    @Override
    public CreatePurchaseHistoryResponse createPurchaseHistory(CreatePurchaseHistoryRequest request) {
        PurchaseHistoryEntity purchaseHistory = PurchaseHistoryEntity.builder()
                .userId(request.getUserId())
                .purchaseDate(LocalDateTime.now())
                .status("COMPLETED")
                .totalAmount(request.getTotalAmount())
                .recordId(request.getRecordId())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .build();

        PurchaseHistoryEntity savedPurchaseHistory = purchaseHistoryRepository.save(purchaseHistory);

        return CreatePurchaseHistoryResponse.builder()
                .id(savedPurchaseHistory.getId())
                .userId(savedPurchaseHistory.getUserId())
                .purchaseDate(savedPurchaseHistory.getPurchaseDate())
                .status(savedPurchaseHistory.getStatus())
                .totalAmount(savedPurchaseHistory.getTotalAmount())
                .recordId(savedPurchaseHistory.getRecordId())
                .quantity(savedPurchaseHistory.getQuantity())
                .price(savedPurchaseHistory.getPrice())
                .build();
    }

    @Override
    public GetPurchaseHistoryResponse getPurchaseHistory(GetPurchaseHistoryRequest request) {
        PurchaseHistoryEntity entity = purchaseHistoryRepository.findById(request.getId())
                .orElseThrow(() -> new PurchaseHistoryNotFoundException("Purchase history not found with id: " + request.getId()));

        return GetPurchaseHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .purchaseDate(entity.getPurchaseDate())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .recordId(entity.getRecordId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }

    @Override
    public List<GetPurchaseHistoryResponse> getAllPurchaseHistories(Long userId) {
        List<PurchaseHistoryEntity> entities = purchaseHistoryRepository.findAllByUserId(userId);
        return entities.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePurchaseHistory(Long id) {
        if (!purchaseHistoryRepository.findById(id).isPresent()) {
            throw new PurchaseHistoryNotFoundException("Purchase history not found with id: " + id);
        }
        purchaseHistoryRepository.deleteById(id);
    }

    private GetPurchaseHistoryResponse convertToGetResponse(PurchaseHistoryEntity entity) {
        return GetPurchaseHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .purchaseDate(entity.getPurchaseDate())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .recordId(entity.getRecordId())
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }
}