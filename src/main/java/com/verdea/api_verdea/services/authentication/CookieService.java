package com.verdea.api_verdea.services.authentication;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.time.Duration;
import java.util.UUID;

@Service
public class CookieService {

    @Value("${jwt.cookie.access-token-name:accessToken}")
    private String accessTokenCookieName;

    @Value("${jwt.cookie.refresh-token-name:refreshToken}")
    private String refreshTokenCookieName;

    @Value("${jwt.ttl}")
    private Duration accessTokenTtl;

    @Value("${jwt.refresh-token.ttl:604800}")  // 7 dias em segundos (padrão)
    private long refreshTokenTtlSeconds;

    @Value("${jwt.cookie.secure:true}")
    private boolean secureCookie;

    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(accessTokenCookieName, accessToken)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(accessTokenTtl.getSeconds())
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, UUID refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(refreshTokenCookieName, refreshToken.toString())
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/auth/refresh-token")
                .maxAge(refreshTokenTtlSeconds)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(accessTokenCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public UUID extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, refreshTokenCookieName);

        if (cookie == null) return null;

        try {
            return UUID.fromString(cookie.getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String extractAccessTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, accessTokenCookieName);

        if (cookie == null) return null;

        try {
            return cookie.getValue();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}