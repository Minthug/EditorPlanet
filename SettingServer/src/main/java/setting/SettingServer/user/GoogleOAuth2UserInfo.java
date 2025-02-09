package setting.SettingServer.user;

import setting.SettingServer.entity.ProviderType;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final String accessToken;
    private final String id;
    private final String email;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String nickname;
    private final String profileImageUrl;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes, String accessToken) {
        this.attributes = attributes;
        this.accessToken = accessToken;
        this.id = (String) attributes.get("sub");
        this.email = (String) attributes.get("email");
        this.name = (String) attributes.get("name");
        this.firstName = (String) attributes.get("given_name");
        this.lastName = (String) attributes.get("family_name");
        this.nickname = null;
        this.profileImageUrl = (String) attributes.get("picture");
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

    @Override
    public ProviderType getProvider() {
        return ProviderType.GOOGLE;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
