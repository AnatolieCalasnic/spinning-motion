package org.myexample.spinningmotion.persistence.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserEntity {
    private Long id;
    private String fname;
    private String lname;
    private String email;
    private String password;
    private String address;
    private String postalCode;
    private String country;
    private String city;
    private String region;
    private String phonenum;
    private boolean isAdmin;
}