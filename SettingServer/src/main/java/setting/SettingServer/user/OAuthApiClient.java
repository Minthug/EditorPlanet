package setting.SettingServer.user;

import setting.SettingServer.entity.ProviderType;

public interface OAuthApiClient {

    ProviderType oAuthProvider();
    String requestAccessToken(OAuthLoginParams params);
    OAuth2UserInfo requestOauthInfo(String accessToken);
}
