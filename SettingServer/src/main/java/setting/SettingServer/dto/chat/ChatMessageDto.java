package setting.SettingServer.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import setting.SettingServer.entity.chat.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageDto(Long id, String content, String type, String senderId, String senderName, LocalDateTime sentAt) {

}