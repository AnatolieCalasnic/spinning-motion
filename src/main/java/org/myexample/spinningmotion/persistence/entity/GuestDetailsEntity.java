package org.myexample.spinningmotion.persistence.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
@Entity
@Table(name = "guest_order")
@Builder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuestDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "purchase_history_id")
    private Long purchaseHistoryId;

    @NotBlank
    @Length(min = 2, max = 50)
    private String fname;

    @NotBlank
    @Length(min = 2, max = 50)
    private String lname;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String address;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;

    @NotBlank
    private String city;

    private String region;

    @NotBlank
    @Length(min = 6, max = 15)
    private String phonenum;
}