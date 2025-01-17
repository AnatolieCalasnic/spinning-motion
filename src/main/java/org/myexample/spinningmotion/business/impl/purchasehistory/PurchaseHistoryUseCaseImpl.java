package org.myexample.spinningmotion.business.impl.purchasehistory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.myexample.spinningmotion.business.exception.InsufficientQuantityException;
import org.myexample.spinningmotion.business.exception.PurchaseHistoryNotFoundException;
import org.myexample.spinningmotion.business.exception.RecordNotFoundException;
import org.myexample.spinningmotion.business.interfc.PurchaseHistoryUseCase;
import org.myexample.spinningmotion.domain.purchase_history.*;
import org.myexample.spinningmotion.persistence.PurchaseHistoryRepository;
import org.myexample.spinningmotion.persistence.RecordRepository;
import org.myexample.spinningmotion.persistence.entity.PurchaseHistoryEntity;
import org.myexample.spinningmotion.persistence.entity.RecordEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseHistoryUseCaseImpl implements PurchaseHistoryUseCase {
    private static final String PURCHASE_HISTORY_NOT_FOUND = "Purchase history not found with id: ";

    private final PurchaseHistoryRepository purchaseHistoryRepository;
    private final RecordRepository recordRepository;

    @Override
    @Transactional
    public CreatePurchaseHistoryResponse createPurchaseHistory(CreatePurchaseHistoryRequest request) {
        if (request.getRecordId() == null || request.getRecordId() <= 0) {
            throw new IllegalArgumentException("Record ID must be a positive non-null value.");
        }
        if (request.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero.");
        }
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        // Find the record and check quantity
        RecordEntity record = recordRepository.findById(request.getRecordId())
                .orElseThrow(() -> new RecordNotFoundException("Record not found with id: " + request.getRecordId()));

        if (record.getQuantity() < request.getQuantity()) {
            throw new InsufficientQuantityException("Insufficient quantity available. Requested: "
                    + request.getQuantity() + ", Available: " + record.getQuantity());
        }

        // Update record quantity
        record.setQuantity(record.getQuantity() - request.getQuantity());
        recordRepository.save(record);

        double finalPrice = request.getPrice();
        double finalTotalAmount = finalPrice * request.getQuantity();
        PurchaseHistoryEntity purchaseHistory = PurchaseHistoryEntity.builder()
                .userId(request.getUserId())
                .isGuest(request.isGuest())
                .purchaseDate(LocalDateTime.now())
                .status("COMPLETED")
                .totalAmount(finalTotalAmount)
                .recordId(request.getRecordId())
                .quantity(request.getQuantity())
                .price(finalPrice)
                .build();

        PurchaseHistoryEntity savedPurchaseHistory = purchaseHistoryRepository.save(purchaseHistory);

        return CreatePurchaseHistoryResponse.builder()
                .id(savedPurchaseHistory.getId())
                .userId(savedPurchaseHistory.getUserId())
                .purchaseDate(savedPurchaseHistory.getPurchaseDate())
                .status(savedPurchaseHistory.getStatus())
                .totalAmount(finalTotalAmount)
                .recordId(savedPurchaseHistory.getRecordId())
                .quantity(savedPurchaseHistory.getQuantity())
                .price(finalPrice)
                .build();
    }

    @Override
    public GetPurchaseHistoryResponse getPurchaseHistory(GetPurchaseHistoryRequest request) {
        PurchaseHistoryEntity referenceOrder = purchaseHistoryRepository.findById(request.getId())
                .orElseThrow(() -> new PurchaseHistoryNotFoundException(PURCHASE_HISTORY_NOT_FOUND  + request.getId()));

        // Converting the reference order to response
        return convertToGetResponse(referenceOrder);
    }
    @Override
    public List<GetPurchaseHistoryResponse> getRelatedOrders(Long orderId) {
        PurchaseHistoryEntity referenceOrder = purchaseHistoryRepository.findById(orderId)
                .orElseThrow(() -> new PurchaseHistoryNotFoundException(PURCHASE_HISTORY_NOT_FOUND + orderId));

        return purchaseHistoryRepository
                .findAllByUserIdAndPurchaseDate(referenceOrder.getUserId(), referenceOrder.getPurchaseDate())
                .stream()
                .map(this::convertToGetResponse)
                .toList();
    }
    @Override
    public List<GetPurchaseHistoryResponse> getAllPurchaseHistories(Long userId) {
        List<PurchaseHistoryEntity> entities = purchaseHistoryRepository.findAllByUserId(userId);
        return entities.stream()
                .map(this::convertToGetResponse)
                .toList();
    }

    @Override
    public void deletePurchaseHistory(Long id) {
        if (!purchaseHistoryRepository.findById(id).isPresent()) {
            throw new PurchaseHistoryNotFoundException(PURCHASE_HISTORY_NOT_FOUND + id);
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
    @Override
    public AdminDashboardStats getAdminDashboardStats() {
        return AdminDashboardStats.builder()
                .totalOrders(countTotalOrders())
                .totalRevenue(calculateTotalRevenue())
                .recentOrders(getRecentPurchaseHistories(10))
                .build();
    }

    @Override
    public PurchaseHistoryStats getPurchaseHistoryStats() {
        return PurchaseHistoryStats.builder()
                .totalOrders(countTotalOrders())
                .totalRevenue(calculateTotalRevenue())
                .purchasesByRecord(getPurchasesByRecord())
                .averageOrderValue(calculateAverageOrderValue())
                .build();
    }

    @Override
    public List<GetPurchaseHistoryResponse> getRecentPurchaseHistories(int limit) {
        List<PurchaseHistoryEntity> recentEntities = purchaseHistoryRepository
                .findTop10ByOrderByPurchaseDateDesc();
        return recentEntities.stream()
                .map(this::convertToGetResponse)
                .toList();
    }

    @Override
    public double calculateTotalRevenue() {
        return purchaseHistoryRepository.findAll().stream()
                .mapToDouble(PurchaseHistoryEntity::getTotalAmount)
                .sum();
    }

    @Override
    public long countTotalOrders() {
        return purchaseHistoryRepository.count();
    }

    @Override
    public Map<String, Integer> getPurchasesByRecord() {
        return purchaseHistoryRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        entity -> entity.getRecordId().toString(),
                        Collectors.summingInt(PurchaseHistoryEntity::getQuantity)
                ));
    }

    private double calculateAverageOrderValue() {
        List<PurchaseHistoryEntity> allOrders = purchaseHistoryRepository.findAll();
        if (allOrders.isEmpty()) {
            return 0.0;
        }
        double totalRevenue = calculateTotalRevenue();
        return totalRevenue / allOrders.size();
    }
    @Override
    public List<GetPurchaseHistoryResponse> getAllPurchaseHistories() {
        List<PurchaseHistoryEntity> entities = purchaseHistoryRepository.findAll();
        return entities.stream()
                .map(this::convertToGetResponse)
                .collect(Collectors.toList());
    }

}