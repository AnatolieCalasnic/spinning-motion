package org.myexample.spinningmotion.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "record")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class RecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "title")
    private String title;

    @NotBlank
    @Column(name = "artist")
    private String artist;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private GenreEntity genre;

    @NotNull
    @Positive
    @Column(name = "price")
    private Double price;

    @Min(1900)
    @Column(name = "release_year")
    private Integer year;

    @NotBlank
    @Column(name = "`condition`")
    private String condition;

    @NotNull
    @Min(0)
    @Column(name = "quantity")
    private Integer quantity;
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecordImageEntity> images = new ArrayList<>();
}