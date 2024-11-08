package org.myexample.spinningmotion.persistence.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "basket_item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasketItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "record_id")
    private Long recordId;

    @NotNull
    @Min(1)
    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "basket_id")
    private BasketEntity basket;
}