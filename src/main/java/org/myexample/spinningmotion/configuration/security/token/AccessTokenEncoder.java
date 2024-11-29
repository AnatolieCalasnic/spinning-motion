package org.myexample.spinningmotion.configuration.security.token;

public interface AccessTokenEncoder {
    String encode(AccessToken accessToken);
}
