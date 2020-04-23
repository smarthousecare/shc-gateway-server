package com.house.care.gatewayserver.config.oauth2;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.util.StringUtils;

/**
 * Helps with OAuth2 cookie handling.
 */
public class OAuth2CookieHelper {

    /**
     * Name of the access token cookie.
     */
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    /**
     * Name of the refresh token cookie in case of remember me.
     */
    public static final String REFRESH_TOKEN_COOKIE = OAuth2AccessToken.REFRESH_TOKEN;

    /**
     * Name of the session-only refresh token in case the user did not check
     * remember me.
     */
    public static final String SESSION_TOKEN_COOKIE = "session_token";

    /**
     * The names of the Cookies we set.
     */
    private static final List<String> COOKIE_NAMES = Arrays.asList(ACCESS_TOKEN_COOKIE, REFRESH_TOKEN_COOKIE,
            SESSION_TOKEN_COOKIE);

    /**
     * Number of seconds to expire refresh token cookies before the enclosed
     * token expires. This makes sure we don't run into race conditions where
     * the cookie is still there but expires while we process it.
     */
    private static final long REFRESH_TOKEN_EXPIRATION_WINDOW_SECS = 3L;

    private final Logger log = LoggerFactory.getLogger(OAuth2CookieHelper.class);

    /**
     * Used to parse JWT claims.
     */
    private JsonParser jsonParser = JsonParserFactory.getJsonParser();

    public static Cookie getAccessTokenCookie(HttpServletRequest request) {

        return getCookie(request, ACCESS_TOKEN_COOKIE);
    }

    public static Cookie getRefreshTokenCookie(HttpServletRequest request) {

        Cookie cookie = getCookie(request, REFRESH_TOKEN_COOKIE);
        if (cookie == null) {
            cookie = getCookie(request, SESSION_TOKEN_COOKIE);
        }
        return cookie;
    }

    /**
     * Get a cookie by name from the given servlet request.
     *
     * @param request
     *            the request containing the cookie.
     * @param cookieName
     *            the case-sensitive name of the cookie to get.
     * @return the resulting Cookie; or null, if not found.
     */
    private static Cookie getCookie(HttpServletRequest request, String cookieName) {

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    String value = cookie.getValue();
                    if (StringUtils.hasText(value)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }

    public void createCookies(HttpServletRequest request, String accessToken, String refreshToken,
            OAuth2Cookies result) {

        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        accessTokenCookie.setHttpOnly(true);
        Cookie refreshTokeCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);
        refreshTokeCookie.setHttpOnly(true);
        setCookieProperties(accessTokenCookie, request.isSecure(), null);
        setCookieProperties(refreshTokeCookie, request.isSecure(), null);
        log.debug("created access token cookie '{}'", accessTokenCookie.getName());

        result.setCookies(accessTokenCookie, refreshTokeCookie);
    }

    /**
     * Retrieve the given claim from the given token.
     *
     * @param refreshToken
     *            the JWT token to examine.
     * @param claimName
     *            name of the claim to get.
     * @param clazz
     *            the Class we expect to find there.
     * @return the desired claim.
     * @throws InvalidTokenException
     *             if we cannot find the claim in the token or it is of wrong
     *             type.
     */
    @SuppressWarnings("unchecked")
    private <T> T getClaim(String refreshToken, String claimName, Class<T> clazz) {

        Jwt jwt = JwtHelper.decode(refreshToken);
        String claims = jwt.getClaims();
        Map<String, Object> claimsMap = jsonParser.parseMap(claims);
        Object claimValue = claimsMap.get(claimName);
        if (claimValue == null) {
            return null;
        }
        if (!clazz.isAssignableFrom(claimValue.getClass())) {
            throw new InvalidTokenException("claim is not of expected type: " + claimName);
        }
        return (T) claimValue;
    }

    /**
     * Set cookie properties of access and refresh tokens.
     *
     * @param cookie
     *            the cookie to modify.
     * @param isSecure
     *            whether it is coming from a secure request.
     * @param domain
     *            the domain for which the cookie is valid. If null, then will
     *            fall back to default.
     */
    private void setCookieProperties(Cookie cookie, boolean isSecure, String domain) {

        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setSecure(isSecure); // if the request comes per HTTPS set the
                                    // secure option on the cookie
        if (domain != null) {
            cookie.setDomain(domain);
        }
    }

    /**
     * Logs the user out by clearing all cookies.
     *
     * @param httpServletRequest
     *            the request containing the Cookies.
     * @param httpServletResponse
     *            the response used to clear them.
     */
    public void clearCookies(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        for (String cookieName : COOKIE_NAMES) {
            clearCookie(httpServletRequest, httpServletResponse, null, cookieName);
        }
    }

    private void clearCookie(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
            String domain, String cookieName) {

        Cookie cookie = new Cookie(cookieName, "");
        setCookieProperties(cookie, httpServletRequest.isSecure(), domain);
        cookie.setMaxAge(0);
        httpServletResponse.addCookie(cookie);
        log.debug("clearing cookie {}", cookie.getName());
    }

    /**
     * Strip our token cookies from the array.
     *
     * @param cookies
     *            the cookies we receive as input.
     * @return the new cookie array without our tokens.
     */
    public Cookie[] stripCookies(Cookie[] cookies) {

        CookieCollection cc = new CookieCollection(cookies);
        if (cc.removeAll(COOKIE_NAMES)) {
            return cc.toArray();
        }
        return cookies;
    }

    public Cookie[] stripCookies(Cookie[] cookies, OAuth2Cookies oAuth2Cookies) {

        CookieCollection cc = new CookieCollection(cookies);
        if (cc.removeAll(COOKIE_NAMES)) {
            return cc.toArray();
        }
        if (oAuth2Cookies != null) {
            cc.add(oAuth2Cookies.getAccessTokenCookie());
            cc.add(oAuth2Cookies.getRefreshToken());
            return cc.toArray();
        }
        return cookies;
    }
}
