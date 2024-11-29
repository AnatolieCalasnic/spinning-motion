package org.myexample.spinningmotion.configuration.security.token;

public interface AccessToken {
    String getSubject();
    Boolean getIsAdmin();
    Long getUserId();
}
