package org.myexample.spinningmotion.configuration.security.token.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.myexample.spinningmotion.configuration.security.token.AccessToken;

@EqualsAndHashCode
@Getter
public class AccessTokenImpl implements AccessToken {
    private final String subject;
    private final Long userId;
    private final Boolean isAdmin;

    public AccessTokenImpl(String subject, Long userId, Boolean isAdmin) {
        this.subject = subject;
        this.userId = userId;
        this.isAdmin = isAdmin;
    }
}