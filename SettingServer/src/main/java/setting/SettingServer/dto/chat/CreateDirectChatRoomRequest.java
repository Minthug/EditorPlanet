package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * 채팅방 생성 요청 DTO - 1:1 채팅
 * @param targetUserId
 */
public record CreateDirectChatRoomRequest(@NotBlank(message = "상대방 ID는 필수 입니다") String targetUserId) {
}
