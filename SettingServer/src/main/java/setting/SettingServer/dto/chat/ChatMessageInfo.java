package setting.SettingServer.dto.chat;

import lombok.Data;
import setting.SettingServer.entity.chat.ChatMessage;

import java.time.LocalDateTime;

@Data
public class ChatMessageInfo {
    private long chatMessageId;
    private String authorId;
    private String message;
    private LocalDateTime localDateTime;

    public ChatMessageInfo(ChatMessage chatMessage) {
        this.chatMessageId = chatMessage.getId();
        this.authorId = chatMessage.getAuthorId();
        this.message = chatMessage.getMessage();
        this.localDateTime = chatMessage.getCreatedAt();
    }
}
