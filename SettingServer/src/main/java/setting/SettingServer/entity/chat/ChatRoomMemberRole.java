package setting.SettingServer.entity.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomMemberRole {
    OWNER("방장"),
    ADMIN("관리자"),
    MEMBER("일반 멤버");

    private final String description;
}
