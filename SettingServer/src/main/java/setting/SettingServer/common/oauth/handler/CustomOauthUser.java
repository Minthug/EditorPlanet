package setting.SettingServer.common.oauth.handler;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import setting.SettingServer.entity.UserRole;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOauthUser extends DefaultOAuth2User {

    private String email;
    private UserRole role;

    public CustomOauthUser(Collection<? extends GrantedAuthority> authorities,
                           Map<String, Object> attributes, String nameAttributeKey,
                           String email, UserRole role) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.role = role;
    }
}
