package org.myexample.spinningmotion.business.impl;

import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.PurchaseItemEntity;
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
        PurchaseHistoryEntity entity = PurchaseHistoryEntity.builder()
                .userId(request.getUserId())
                .purchaseDate(LocalDateTime.now())
                .status("COMPLETED")
                .totalAmount(calculateTotalAmount(request.getItems()))
                .items(convertToPurchaseItemEntities(request.getItems()))
                .build();

        PurchaseHistoryEntity savedEntity = purchaseHistoryRepository.save(entity);
        return convertToCreateResponse(savedEntity);
    }

    @Override
    public GetPurchaseHistoryResponse getPurchaseHistory(GetPurchaseHistoryRequest request) {
        PurchaseHistoryEntity entity = purchaseHistoryRepository.findById(request.getId())
                .orElseThrow(() -> new PurchaseHistoryNotFoundException("Purchase history not found with id: " + request.getId()));
        return convertToGetResponse(entity);
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

    private Double calculateTotalAmount(List<PurchaseItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    private List<PurchaseItemEntity> convertToPurchaseItemEntities(List<PurchaseItem> items) {
        return items.stream()
                .map(item -> PurchaseItemEntity.builder()
                        .recordId(item.getRecordId())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    private CreatePurchaseHistoryResponse convertToCreateResponse(PurchaseHistoryEntity entity) {
        return CreatePurchaseHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .purchaseDate(entity.getPurchaseDate())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .items(convertToPurchaseItems(entity.getItems()))
                .build();
    }

    private GetPurchaseHistoryResponse convertToGetResponse(PurchaseHistoryEntity entity) {
        return GetPurchaseHistoryResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .purchaseDate(entity.getPurchaseDate())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .items(convertToPurchaseItems(entity.getItems()))
                .build();
    }

    private List<PurchaseItem> convertToPurchaseItems(List<PurchaseItemEntity> itemEntities) {
        return itemEntities.stream()
                .map(entity -> PurchaseItem.builder()
                        .recordId(entity.getRecordId())
                        .quantity(entity.getQuantity())
                        .price(entity.getPrice())
                        .build())
                .collect(Collectors.toList());
    }
}