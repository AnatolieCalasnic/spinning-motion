package org.myexample.spinningmotion.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_guest")
    private boolean isGuest;

    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    @NotNull
    @Column(name = "status")
    private String status;

    @NotNull
    @Positive
    @Column(name = "total_amount")
    private Double totalAmount;

    @NotNull
    @Min(1)
    @Column(name = "record_id")
    private Long recordId;

    @NotNull
    @Min(1)
    @Column(name = "quantity")
    private Integer quantity;

    @NotNull
    @Positive
    @Column(name = "price")
    private Double price;
}

