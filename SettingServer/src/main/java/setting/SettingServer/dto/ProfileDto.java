package setting.SettingServer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import setting.SettingServer.entity.Member;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Long id;
    private String email;
    private String name;
    private String profileImage;

    public static ProfileDto fromMember(Member member) {
        String imageUrl = member.getImageUrl();
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageUrl = "";
        }
        return new ProfileDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                imageUrl
        );
    }
}
