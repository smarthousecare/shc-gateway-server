package com.house.care.gatewayserver.config.oauth2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class OAuth2Cookies {

    private Cookie accessTokenCookie;

    private Cookie refreshToken;

    public Cookie getAccessTokenCookie() {

        return accessTokenCookie;
    }

    public Cookie getRefreshToken() {

        return refreshToken;
    }

    public void setCookies(Cookie accessTokenCookie, Cookie refreshToken) {

        this.accessTokenCookie = accessTokenCookie;
        this.refreshToken = refreshToken;
    }

    void addCookiesTo(HttpServletResponse response) {

        response.addCookie(getAccessTokenCookie());
        response.addCookie(getRefreshToken());
    }
}
