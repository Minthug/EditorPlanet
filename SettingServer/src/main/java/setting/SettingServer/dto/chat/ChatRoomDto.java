package setting.SettingServer.dto.chat;

import java.time.LocalDateTime;

/**
 * 채팅방 기본 정보 DTO
 * @param id
 * @param name
 * @param type DIRECT || GROUP
 * @param memberCount
 * @param latestMessageContent
 * @param latestMessageTime
 * @param latestMessageSenderId
 * @param latestMessageSenderName
 */
public record ChatRoomDto(String id, String name, String type, int memberCount,
                          String latestMessageContent, LocalDateTime latestMessageTime,
                          String latestMessageSenderId, String latestMessageSenderName) {
}
