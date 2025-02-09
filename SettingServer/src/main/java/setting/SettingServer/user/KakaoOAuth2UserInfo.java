package setting.SettingServer.user;

import setting.SettingServer.entity.ProviderType;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String nickname;
    private final String profileImageUrl;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes, String accessToken) {
        this.accessToken = accessToken;

        // attributes 맵의 kakao_account 키의 값에 실제 attributes 맵이 할당되어 있다함.
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) attributes.get("profile");
        this.attributes = kakaoProfile;
        this.id = ((Long) attributes.get("id")).toString();
        this.email = (String) kakaoAccount.get("email");

        this.name = null;
        this.firstName = null;
        this.lastName = null;

        this.nickname = (String) attributes.get("nickname");
        this.profileImageUrl = (String) attributes.get("profile_image_url");

        this.attributes.put("id", id);
        this.attributes.put("email", email);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public ProviderType getProvider() {
        return ProviderType.KAKAO;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}