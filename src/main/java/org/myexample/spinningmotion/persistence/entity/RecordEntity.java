package org.myexample.spinningmotion.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @NotBlank(message = "Record's title is required")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Artist name is required")
    @Column(name = "artist")
    private String artist;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    private GenreEntity genre;

    @NotNull
    @Positive
    @Column(name = "price")
    private Double price;

    @Min(value = 1900, message = "Release year must be 1900 or later")
    @Column(name = "release_year")
    private Integer year;

    @NotBlank
    @Column(name = "`condition`")
    private String condition;

    @NotNull
    @Min(value = 0, message = "Quantity cannot be negative")
    @Column(name = "quantity")
    private Integer quantity;

    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RecordImageEntity> images = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}