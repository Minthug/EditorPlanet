package setting.SettingServer.config.jwt.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import setting.SettingServer.config.jwt.service.JwtService;
import setting.SettingServer.entity.JwtTokenType;
import setting.SettingServer.repository.MemberRepository;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Value("${spring.security.jwt.access-expiration}")
    private String accessTokenExpiration;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = extractUsername(authentication);
        String accessToken = jwtService.createToken(email, JwtTokenType.ACCESS);
        String refreshToken = jwtService.createToken(email, JwtTokenType.REFRESH);

        jwtService.sendAccessAndRefreshToken(response, accessToken, refreshToken);

        memberRepository.findByEmail(email)
                .ifPresent(user -> {
                    user.updateRefreshToken(refreshToken);
                    memberRepository.saveAndFlush(user);
                });

        log.info("Login Success. email: {}", email);
        log.info("Login Success. accessToken: {}", accessToken);
        log.info("발급된 Access Token 만료 기간: {}", accessTokenExpiration);
    }

    private String extractUsername(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        return userDetails.getUsername();
    }
}
