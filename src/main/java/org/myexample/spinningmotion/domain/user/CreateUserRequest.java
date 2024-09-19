package org.myexample.spinningmotion.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
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
}