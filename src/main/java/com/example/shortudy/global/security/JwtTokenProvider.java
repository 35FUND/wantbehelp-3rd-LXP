package com.example.shortudy.global.security;

import com.example.shortudy.domain.user.entity.UserRole;
import com.example.shortudy.global.error.BaseException;
import com.example.shortudy.global.error.ErrorCode;
import com.example.shortudy.global.security.principal.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 * JJWT 0.12.x 최신 API를 사용하도록 리팩토링됨
 */
@Component
public class JwtTokenProvider {

    private final CustomUserDetailsService customUserDetailsService;
    private final UserDetailsService userDetailsService;

    public JwtTokenProvider(CustomUserDetailsService customUserDetailsService, UserDetailsService userDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
        this.userDetailsService = userDetailsService;
    }

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(Long userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public void validateToken(String token) {
        try {
            parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BaseException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String email = claims.get("email", String.class);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities()
        );
    }
}
