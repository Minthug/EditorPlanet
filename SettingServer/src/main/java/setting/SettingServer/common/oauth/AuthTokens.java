package setting.SettingServer.common.oauth;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthTokens {

    private String accessToken;
    private String refreshToken;
    private String grantType;
    private Long expiresIn;

    public static AuthTokens of(String accessToken, String refreshToken, String grantType, Long expiresIn) {
        return AuthTokens.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .grantType(grantType)
                .expiresIn(expiresIn)
                .build();
    }

    public static AuthTokens fromOAuth2Response(String accessToken, String refreshToken, String grantType, Long expiresIn) {
        return of(accessToken, refreshToken, grantType, expiresIn);
    }

    public static AuthTokens fromJwt(String accessToken, String refreshToken) {
        return of(accessToken, refreshToken, "Bearer ", 3600L);
    }
}
