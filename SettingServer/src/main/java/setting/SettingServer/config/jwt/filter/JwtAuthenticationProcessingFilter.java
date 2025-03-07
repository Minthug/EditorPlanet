package setting.SettingServer.config.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import setting.SettingServer.common.exception.JwtAuthenticationException;
import setting.SettingServer.common.oauth.JwtAuthenticationToken;
import setting.SettingServer.config.jwt.service.JwtService;
import setting.SettingServer.entity.JwtTokenType;
import setting.SettingServer.entity.Member;
import setting.SettingServer.repository.MemberRepository;

import java.io.IOException;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Set<String> NO_CHECK_URL = new HashSet<>(Arrays.asList(
            "/login", "/signup", "/refresh", "/v1/oauth/authorization/{provider}",
            "/v1/oauth/oauth2/authorization/{provider}", "/v1/oauth/{provider}",
            "/v1/oauth/{type}"));

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 인증 예외 URL은 필터 건너뛰기
        if (isNoCheckUrl(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 액세스 토큰 추출 및 관리
            // 일반 API 요청에선 액세스 토큰만 검증하고 처리하도록 단순화
            String accessToken = resolveToken(request);
            if (StringUtils.hasText(accessToken) && jwtService.validateToken(accessToken)) {
                // accessToken 검증 및 인증 처리
                Authentication authentication = getAuthenticationFromToken(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            handlerFilterException(response, e);
        }
    }

    private Authentication getAuthenticationFromToken(String accessToken) {
        String email = jwtService.extractEmail(accessToken)
                .orElseThrow(() -> new JwtAuthenticationException("Could not extract email from token"));

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new JwtAuthenticationException("User not found: " + email));

        UserDetails userDetails = createUserDetails(member);
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private void processAccessToken(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String accessToken) throws IOException, ServletException{
        if (jwtService.validateToken(accessToken)) {
            jwtService.extractAccessToken(request)
                    .filter(jwtService::validateToken)
                    .ifPresent(this::authenticationUser);

        }

        filterChain.doFilter(request, response);
    }

    private void processRefreshToken(HttpServletResponse response, String refreshToken) {
        if (jwtService.validateToken(refreshToken)) {
            jwtService.extractEmail(refreshToken)
                            .flatMap(memberRepository::findByEmail)
                    .ifPresentOrElse(
                    user -> {
                        if (refreshToken.equals(user.getRefreshToken())) {
                            String newAccessToken = jwtService.createToken(user.getEmail(), JwtTokenType.ACCESS);
                            String newRefreshToken = jwtService.createToken(user.getEmail(), JwtTokenType.REFRESH);
                            user.updateRefreshToken(newRefreshToken);
                            memberRepository.save(user);
                            jwtService.sendAccessAndRefreshToken(response, newAccessToken, newRefreshToken);
                        } else {
                            throw new JwtAuthenticationException("Refresh token doesn't match");
                        }
                    },
                    () -> {
                        throw new JwtAuthenticationException("User not found for the given refresh token");
                    }
            );
        } else {
            throw new JwtAuthenticationException("Invalid refresh token");
        }
    }


    private void authenticationUser(String accessToken) {
        jwtService.extractEmail(accessToken)
                .flatMap(memberRepository::findByEmail)
                .ifPresent(this::saveAuthentication);
    }

    private void saveAuthentication(Member member) {
        UserDetails userDetails = createUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private boolean isNoCheckUrl(String requestURI) {
        return NO_CHECK_URL.contains(requestURI);
    }

    private UserDetails createUserDetails(Member member) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getEmail())
                .password("")
                .authorities(member.getRole().name())
                .build();
    }

    private void handlerFilterException(HttpServletResponse response, Exception e) throws IOException {
        log.error("Jwt Authentication error: {}", e);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
    }

    public void setAuthentication(Member member) {
        UserDetails userDetails = createUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
