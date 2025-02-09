package setting.SettingServer.dto.chat;

import lombok.Data;
import setting.SettingServer.entity.Member;

@Data
public class ChatUserInfoDto {
    private String userId;
    private String profileImage;

    public ChatUserInfoDto(Member member) {
        this.userId = String.valueOf(member.getId());
        this.profileImage = member.getImageUrl();
    }
}
