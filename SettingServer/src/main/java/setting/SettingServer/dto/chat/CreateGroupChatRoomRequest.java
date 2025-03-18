package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 채팅방 생성 요청 DTO - GROUP CHAT
 * @param name
 * @param memberIds
 */
public record CreateGroupChatRoomRequest(String name, @NotEmpty(message = "최소 1명 이상의 멤버가 필요합니다")List<String> memberIds) {
}
