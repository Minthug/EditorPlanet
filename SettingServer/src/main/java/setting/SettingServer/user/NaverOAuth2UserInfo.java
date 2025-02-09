package setting.SettingServer.user;

import setting.SettingServer.entity.ProviderType;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String nickname;
    private final String profileImageUrl;

    public NaverOAuth2UserInfo(Map<String, Object> attributes, String accessToken) {
        this.accessToken = accessToken;
        this.attributes = (Map<String, Object>) attributes.get("response");
        this.id = (String) this.attributes.get("id");
        this.email = (String) this.attributes.get("email");
        this.name = (String) this.attributes.get("name");
        this.firstName = null;
        this.lastName = null;
        this.nickname = (String) attributes.get("nickname");

        this.profileImageUrl = (String) attributes.get("profile_image_url");
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
        return ProviderType.NAVER;
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
