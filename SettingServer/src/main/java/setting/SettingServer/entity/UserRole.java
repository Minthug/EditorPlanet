package setting.SettingServer.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    GUEST("ROLE_GUEST"),
    USER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN"),
    SOCIAL("ROLE_SOCIAL");

    private final String key;
}
