package org.myexample.spinningmotion.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriber")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email must be included")
    @Email
    @Column(unique = true)
    private String email;

    @Column(name = "subscribed_at")
    private LocalDateTime subscribedAt;

    @PrePersist
    protected void onCreate() {
        subscribedAt = LocalDateTime.now();
    }
}
