package setting.SettingServer.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import setting.SettingServer.entity.chat.ChatMessage;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 DTO
 * @param id
 * @param content
 * @param type CHAT || SYSTEM
 * @param senderId
 * @param senderName
 * @param sentAt
 */
public record ChatMessageDto(Long id, String content, String type, String senderId, String senderName, LocalDateTime sentAt) {

}