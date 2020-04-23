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

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

/**
 * Identity Server Client Class.
 *
 */
@Component
public class IdentityServerClient extends WebServiceGatewaySupport {

    private static final Logger log = LoggerFactory.getLogger(IdentityServerClient.class);

    /**
     * Get User info Method.
     *
     * @param httpRequest
     *            the HttpServlerRequest.
     * @param username
     *            the username.
     * @return user info response.
     */
    public ResponseEntity<String> getUserInfo(HttpServletRequest httpRequest, String username) {

        String userInfoResponseDTO = username == null ? JWTSecurityUtils.getUserByToken(httpRequest) : username;
        return new ResponseEntity<>(userInfoResponseDTO, HttpStatus.OK);
    }
}
