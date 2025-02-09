package setting.SettingServer.user.params;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import setting.SettingServer.entity.ProviderType;
import setting.SettingServer.user.OAuthLoginParams;

@Getter
@NoArgsConstructor
public class KakaoLoginParams implements OAuthLoginParams {

    private String authorizationCode;
    private String email;
    private String name;

    @Override
    public ProviderType oAuthProvider() {
        return ProviderType.KAKAO;
    }

    @Override
    public MultiValueMap<String, String> makeBody() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authorizationCode);
        return body;
    }
}
