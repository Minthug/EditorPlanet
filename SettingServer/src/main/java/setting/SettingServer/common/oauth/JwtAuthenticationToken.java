package setting.SettingServer.common.oauth;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final String email;
    private final Long userId;
    private final String token;
    private final Map<String, Object> attributes;

    public JwtAuthenticationToken(String email, Long userId, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.attributes = new HashMap<>();
        setAuthenticated(true);
    }

    public JwtAuthenticationToken(String email, Long userId, String token, Map<String, Object> attributes,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.attributes = attributes;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
