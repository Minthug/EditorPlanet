package setting.SettingServer.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import setting.SettingServer.entity.chat.ChatMessage;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {

    private String roomId;
    private String authorId;
    private String message;

    public ChatMessage toEntity() {
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(roomId)
                .authorId(authorId)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        return chatMessage;
    }
}
