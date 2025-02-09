package setting.SettingServer.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.ProviderType;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class MemberDto implements Serializable {

    private Long id;
    private String email;
    private String name;
    private String imageUrl;
    private ProviderType providerType;

    public static MemberDto toDto(Member member) {
        if (member.getType() == ProviderType.GOOGLE ||
            member.getType() == ProviderType.KAKAO ||
            member.getType() == ProviderType.NAVER) {
            member.updateProfileImageUrl(member.getImageUrl());
        } else if (member.getImageUrl() == null || member.getImageUrl().isEmpty()) {
            String defaultImageUrl = "";
            member.updateProfileImageUrl(defaultImageUrl);
        }

        return new MemberDto(member.getId(), member.getEmail(),
                member.getName(), member.getImageUrl(), member.getType());
    }
}
