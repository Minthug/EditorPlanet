package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅방 이름 변경 요청 DTO
 * @param name
 */
public record UpdateChatRoomNameRequest(@NotNull(message = "사용자 ID는 필수 입니다") Long userId,
                                        @NotBlank(message = "채팅방 이름은 필수 입니다") String name) {
}
