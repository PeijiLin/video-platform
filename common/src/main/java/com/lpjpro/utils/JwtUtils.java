package com.lpjpro.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Slf4j
@Component
public class JwtUtils {
    @Value("${jwt.sign-key:hzy123}")
    private String signKey;

    @Value("${jwt.access-token-expire:3600000}")
    private Long accessTokenExpire;

    @Value("${jwt.refresh-token-expire:604800000}")
    private Long refreshTokenExpire;

    public static String generateAccessToken(Map<String,Object> claims, String signKey, Long expire) {
        String jwt = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return jwt;
    }

    public static String generateRefreshToken(Map<String,Object> claims, String signKey, Long expire) {
        String jwt = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .compact();
        return jwt;
    }

    public static Claims parseJwt(String jwt, String signKey) {
        Claims claims = Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }

    public static String getUserIdFromToken(String accessToken, String signKey) {
        Claims claims = parseJwt(accessToken, signKey);
        Object o = claims.get("userId");
        String userId = o instanceof Long ? String.valueOf((Long) o) : String.valueOf(o);
        return userId;
    }
}