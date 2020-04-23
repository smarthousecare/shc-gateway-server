/*
 * Copyright 2017 by Brisa Inovação e Tecnologia S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Brisa, SA ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Brisa.
 */
package com.house.care.gatewayserver.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;

/**
 * JWTSecurityUtils Class.
 *
 */
public class JWTSecurityUtils {

    public static final String SUB = "sub";

    public static final String AUTHORIZATION = "Authorization";

    private final static Logger log = LoggerFactory.getLogger(JWTSecurityUtils.class);

    protected static JsonParser objectMapper = JsonParserFactory.create();

    protected static String getToken(HttpServletRequest request) {

        log.debug("Extracting session token");

        String authHeader = request.getHeader(AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    protected static Map<String, Object> decode(String token) {

        log.debug("Decode token: {}", token);

        try {
            Jwt jwt = JwtHelper.decode(token);
            String content = jwt.getClaims();
            Map<String, Object> map = objectMapper.parseMap(content);
            if (map.containsKey("exp") && map.get("exp") instanceof Integer) {
                Integer intValue = (Integer) map.get("exp");
                map.put("exp", new Long(intValue.intValue()));
            }
            return map;
        }
        catch (Exception e) {
            throw new InvalidTokenException("Cannot convert access token to JSON", e);
        }
    }

    public static String getUserByToken(HttpServletRequest request) {

        log.debug("Extract token user.");
        String token = JWTSecurityUtils.getToken(request);
        Map<String, Object> map = JWTSecurityUtils.decode(token);
        log.debug("Token user: {}", map.get(SUB).toString());
        return map.get(SUB).toString();
    }

}
