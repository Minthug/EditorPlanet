package setting.SettingServer.dto.chat;

import lombok.Getter;

@Getter
public class CreatedChatRoomResponse {

    private String roomMakerId;
    private String guestId;
    private String chatRoomId;

    public CreatedChatRoomResponse(String roomMakerId, String guestId, String chatRoomId) {
        this.roomMakerId = roomMakerId;
        this.guestId = guestId;
        this.chatRoomId = chatRoomId;
    }
}
