package setting.SettingServer.config.jwt.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import setting.SettingServer.common.exception.InvalidTokenException;
import setting.SettingServer.common.exception.UserNotFoundException;
import setting.SettingServer.config.jwt.dto.TokenDto;
import setting.SettingServer.entity.JwtTokenType;
import setting.SettingServer.entity.Member;
import setting.SettingServer.repository.MemberRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.Key;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtService {
    // Constants
    private static final String ACCESS_TOKEN_SUBJECT = "accessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "refreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER_PREFIX = "Bearer ";

    // Configuration properties
    @Value("${spring.security.jwt.secret}")
    private String secret;

    @Value("${spring.security.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${spring.security.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${spring.security.jwt.header}")
    private String accessHeader;

    @Value("${spring.security.jwt.refresh.header}")
    private String refreshHeader;

    // Dependencies
    private final MemberRepository memberRepository;
    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return JwtTokenType.REFRESH.equals(claims.get(REFRESH_TOKEN_SUBJECT));
        } catch (JwtException e) {
            return false;
        }
    }


    public String extractJwt(final StompHeaderAccessor accessor) {
        return accessor.getFirstNativeHeader("Authorization");
    }

    // Token Creation
    public String createToken(String email, JwtTokenType type) {
        Date now = new Date();
        Date expiration = calculateExpirationDate(now, type);
        String subject = type == JwtTokenType.ACCESS ? ACCESS_TOKEN_SUBJECT : REFRESH_TOKEN_SUBJECT;

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .claim(EMAIL_CLAIM, email)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private Date calculateExpirationDate(Date now, JwtTokenType type) {
        long expirationPeriod = type == JwtTokenType.ACCESS ? accessExpiration : refreshExpiration;
        return new Date(now.getTime() + expirationPeriod);
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        UserDetails user = new User(claims.getSubject(), "", Collections.emptyList());
        return new UsernamePasswordAuthenticationToken(user, "", Collections.emptyList());
    }

    // Token Validation
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Optional<String> extractEmail(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get(EMAIL_CLAIM, String.class));
        } catch (JwtException e) {
            log.error("Failed to extract email from token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return extractTokenFromHeader(request, accessHeader);
    }

    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Stream.of(
                        extractTokenFromHeader(request, refreshHeader),
                        extractTokenFromCookie(request, REFRESH_TOKEN_SUBJECT),
                        Optional.ofNullable(request.getParameter(REFRESH_TOKEN_SUBJECT))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private Optional<String> extractTokenFromHeader(HttpServletRequest request, String headerName) {
        String bearerToken = request.getHeader(headerName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    private Optional<String> extractTokenFromCookie(HttpServletRequest request, String cookieName) {
        return Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> cookieName.equals(cookie.getName()))
                        .findFirst()
                        .map(Cookie::getValue));
    }

    // Token Storage
    public void updateStoredToken(String email, String token, boolean isAccessToken) {
        memberRepository.findByEmail(email)
                .ifPresentOrElse(user -> {
                            if (isAccessToken) user.updateAccessToken(token);
                            else user.updateRefreshToken(token);
                        },
                        () -> log.error("User not found: {}", email));
    }


    public void updateStoredRefreshToken(String email, String refreshToken) {
        memberRepository.findByEmail(email)
                .ifPresentOrElse(
                        user -> {
                            user.updateRefreshToken(refreshToken);
                            memberRepository.save(user);
                        },
                        () -> log.error("User not found for email: {}", email)
                );
    }

    @Transactional
    public TokenDto reissueTokens(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String email = extractEmail(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Could not extract email from refresh token"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for email: " + email));

        if (!refreshToken.equals(member.getRefreshToken())) {
            throw new InvalidTokenException("Refresh token does not match stored token");
        }

        String newAccessToken = createToken(email, JwtTokenType.ACCESS);
        String newRefreshToken = createToken(email, JwtTokenType.REFRESH);
        updateStoredRefreshToken(email, newRefreshToken);

        return new TokenDto(newAccessToken, newRefreshToken);
    }

    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader(accessHeader, "Bearer " + accessToken);

        response.setHeader(refreshHeader, refreshToken);


        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
//        refreshTokenCookie.setSecure(true); HTTPS only
        refreshTokenCookie.setPath("/");
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_OK);

        try {
            String tokenJSON = String.format("{\"accessToken\":\"%s\",\"refreshToken\":\"%s\"}", accessToken, refreshToken);
            response.setContentType("application/json");
            response.getWriter().write(tokenJSON);
        } catch (IOException e) {
            log.error("Error writing tokens to response", e);
        }
    }
}

