package setting.SettingServer.dto.chat;

import lombok.Data;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;

public record ChatRoomListDto(String id, String name, String type, LocalDateTime sentAt,
                              List<ChatRoomMemberDto> members, List<ChatMessageDto> messages) {

    public static ChatRoomListDto
}
