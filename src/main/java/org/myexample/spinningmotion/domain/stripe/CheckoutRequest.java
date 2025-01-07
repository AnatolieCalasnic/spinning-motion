package org.myexample.spinningmotion.domain.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.myexample.spinningmotion.domain.guest_user.GuestDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class CheckoutRequest {
    private List<Item> items;
    private Map<String, String> metadata = new HashMap<>();
    private GuestDetails guestDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long recordId;
        private String title;
        private String artist;
        private double price;
        private int quantity = 1;
        private String condition;
    }
}