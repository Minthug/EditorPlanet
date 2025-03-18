package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * 채팅방 이름 변경 요청 DTO
 * @param name
 */
public record UpdateChatRoomNameRequest(@NotBlank(message = "채팅방 이름은 필수 입니다") String name) {
}
