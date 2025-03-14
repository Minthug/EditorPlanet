package setting.SettingServer.entity.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomMemberStatus {
    ACTIVE("활성"),
    LEFT("나감"),
    KICKED("추방"),
    BLOCKED("차단");

    private final String description;
}
