package org.myexample.spinningmotion.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GenreEnum {
    ROCK("Rock"),
    CLASSICAL("Classical"),
    JAZZ("Jazz"),
    ELECTRONIC("Electronic"),
    HIP_HOP("Hip Hop"),
    COUNTRY("Country"),
    BLUES("Blues"),
    REGGAE("Reggae"),
    FOLK("Folk"),
    POP("Pop");

    private final String displayName;
}