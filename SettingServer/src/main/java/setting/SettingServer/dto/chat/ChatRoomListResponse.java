package setting.SettingServer.dto.chat;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatRoomListResponse {
    private int page;
    private int count;
    private String reqUserId;
    private List<ChatRoomList> chatRooms;

}
