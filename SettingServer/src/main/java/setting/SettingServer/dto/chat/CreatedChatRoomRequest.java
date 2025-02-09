package setting.SettingServer.dto.chat;

import lombok.Getter;

@Getter
public class CreatedChatRoomRequest {
    private String roomMakerId;
    private String guestId;
}
