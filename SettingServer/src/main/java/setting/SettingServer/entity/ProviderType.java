package setting.SettingServer.entity;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderType {

    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver"),
    LOCAL("local");

    private final String registrationId;
}
