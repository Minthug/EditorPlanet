package setting.SettingServer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import setting.SettingServer.entity.Member;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {

    private Long id;
    private String email;
    private String name;
    private String imageUrl;

    public static MemberResponseDto of(Member member) {
        return new MemberResponseDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getImageUrl()
        );
    }
}
