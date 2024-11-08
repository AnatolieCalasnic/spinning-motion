package org.myexample.spinningmotion.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
@Entity
@Table(name = "app_user")
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Length(min = 2, max = 50)
    @Column(name = "fname")
    private String fname;

    @NotBlank
    @Length(min = 2, max = 50)
    @Column(name = "lname")
    private String lname;

    @NotBlank
    @Email
    @Column(name = "email", unique = true)
    private String email;

    @NotBlank
    @Column(name = "password")
    private String password;

    @NotBlank
    @Column(name = "address")
    private String address;

    @NotBlank
    @Column(name = "postal_code")
    private String postalCode;

    @NotBlank
    @Column(name = "country")
    private String country;

    @NotBlank
    @Column(name = "city")
    private String city;

    @Column(name = "region")
    private String region;

    @NotBlank
    @Length(min = 6, max = 15)
    @Column(name = "phone_number")
    private String phonenum;

    @NotNull
    @Column(name = "is_admin")
    private Boolean isAdmin;
}