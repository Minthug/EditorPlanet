package setting.SettingServer.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import setting.SettingServer.common.exception.OAuth2AuthenticationProcessingException;
import setting.SettingServer.entity.ProviderType;

@RequiredArgsConstructor
@Component
public class OAuth2UserUnlinkManager {

    private final GoogleOAuth2UserUnlink googleOAuth2UserUnlink;
    private final KakaoOAuth2UserUnlink kakaoOAuth2UserUnlink;
    private final NaverOAuth2UserUnlink naverOAuth2UserUnlink;

    public void unlink(ProviderType provider, String accessToken) {
        if (ProviderType.GOOGLE.equals(provider)) {
            googleOAuth2UserUnlink.unlink(accessToken);
        } else if (ProviderType.KAKAO.equals(provider)) {
            kakaoOAuth2UserUnlink.unlink(accessToken);
        } else if (ProviderType.NAVER.equals(provider)){
            naverOAuth2UserUnlink.unlink(accessToken);
        } else {
            throw new OAuth2AuthenticationProcessingException(
                    "Unlink with " + provider.getRegistrationId() + " is not supported"
            );
        }
    }
}
