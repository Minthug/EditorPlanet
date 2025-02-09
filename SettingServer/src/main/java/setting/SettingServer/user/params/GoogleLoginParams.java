package setting.SettingServer.user.params;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import setting.SettingServer.entity.ProviderType;
import setting.SettingServer.user.OAuthLoginParams;

@Getter
@NoArgsConstructor
public class GoogleLoginParams implements OAuthLoginParams {

    private String authorizationCode;
    private String email;
    private String name;

    public GoogleLoginParams(String code) {
        this.authorizationCode = code;
    }

    @Override
    public ProviderType oAuthProvider() {
        return ProviderType.GOOGLE;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        return body;
    }
}
