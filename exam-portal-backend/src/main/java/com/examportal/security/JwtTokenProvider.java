package com.examportal.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.*;
import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {

        byte[] keyBytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(jwtSecret.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //Generate access token from Authentication
    public String generateToken(Authentication authentication) {
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        String roles = principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("roles",roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    // Generate token directly from email (OAuth2 / refresh)
    public String generateTokenFromEmail(String email){
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    //Generate refresh token
    public String generateRefreshToken(String email){
        return Jwts.builder()
                .subject(email)
                .claim("type","refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey())
                .compact();
    }
//Extract email/username from token
    public String getEmailFromToken(String token){
        return parseClaims(token).getSubject();
    }

    // Validate token
    public boolean validateToken(String token){
        try{
            parseClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Malformed JWT: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT : {}",e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT : {}",e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Empty JWT claims : {}",e.getMessage());
        }
        return  false;
    }

    //Check if token is expired
    public boolean isTokenExpired(String token){
        try{
            Date expiry = parseClaims(token).getExpiration();
            return expiry.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    //Get remaining validity ms
    public long getTokenRemainingMs(String token){
        Date expiry = parseClaims(token).getExpiration();
        return expiry.getTime() - System.currentTimeMillis();
    }

    private Claims parseClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
