package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 채팅방 생성 요청 DTO - GROUP CHAT
 * @param name
 * @param memberIds
 */
public record CreateGroupChatRoomRequest(String name, @NotNull(message = "생성자 ID는 필수 입니다") Long creatorId, @NotEmpty(message = "최소 1명 이상의 멤버가 필요합니다")List<Long> memberIds) {
}
