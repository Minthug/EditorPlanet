package setting.SettingServer.dto.chat;

import java.time.LocalDateTime;

public record ChatRoomDto(String id, String name, String type, int memberCount,
                          String latestMessageContent, LocalDateTime latestMessageTime,
                          String latestMessageSenderId, String latestMessageSenderName) {
}
