package org.myexample.spinningmotion.domain.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
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
