package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅방 생성 요청 DTO - 1:1 채팅
 * @param targetUserId
 */
public record CreateDirectChatRoomRequest(@NotNull(message = "사용자 ID는 필수 입니다") Long userId, @NotNull(message = "상대방 ID는 필수 입니다") Long targetUserId) {
}
