package setting.SettingServer.common.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class AuthTokenGenerator {
    private static final String BEARER_TYPE = "Bearer ";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60; // 1 hour
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24; // 1 day

    private final JwtTokenProvider jwtTokenProvider;

    public AuthTokens generate(Long memberId) {
        long now = (new Date()).getTime();
        Date accessExpiredAt = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        Date refreshExpiredAt = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        String subject = memberId.toString();
        String accessToken = jwtTokenProvider.generate(subject, accessExpiredAt);
        String refreshToken = jwtTokenProvider.generate(subject, refreshExpiredAt);

        return AuthTokens.of(accessToken, refreshToken, BEARER_TYPE, ACCESS_TOKEN_EXPIRE_TIME / 1000L);
    }

    public long extractMemberId(String accessToken) {
        return Long.valueOf(jwtTokenProvider.extractSubject(accessToken));
    }
}