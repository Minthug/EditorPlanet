package setting.SettingServer.dto.chat;

import java.time.LocalDateTime;

public record ChatContractResponse(long contractId, String contractName, String profileImage,
                                   String latestMessage, LocalDateTime latestMessageTime, long unreadCount) {
}
