package org.myexample.spinningmotion.domain.guest_user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuestDetails {
    private Long id;
    private Long purchaseHistoryId;
    private String fname;
    private String lname;
    private String email;
    private String address;
    private String postalCode;
    private String country;
    private String city;
    private String region;
    private String phonenum;
}
