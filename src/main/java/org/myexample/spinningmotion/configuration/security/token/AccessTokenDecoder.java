package org.myexample.spinningmotion.configuration.security.token;


public interface AccessTokenDecoder {
    AccessToken decode(String accessTokenEncoded);
}
