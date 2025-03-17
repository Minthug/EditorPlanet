package setting.SettingServer.dto.chat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record ChatRoomDetailDto(String id, String name, String type, LocalDateTime sentAt,
                                List<ChatRoomMemberDto> members, List<ChatMessageDto> messages) {


    public ChatRoomDetailDto(List<ChatRoomMemberDto> members, LocalDateTime sentAt, String type, String name, String id) {
        this(id, name, type, sentAt, members, new ArrayList<>());
    }

    public ChatRoomDetailDto withMessages(List<ChatMessageDto> messages) {
        return new ChatRoomDetailDto(id, name, type, sentAt, members, messages);
    }
}
