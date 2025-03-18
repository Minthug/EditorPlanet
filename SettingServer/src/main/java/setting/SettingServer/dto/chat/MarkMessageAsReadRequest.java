package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotBlank;

/**
 * 메시지 읽음 처리 요청 DTO
 * @param messageId
 */
public record MarkMessageAsReadRequest(@NotBlank(message = "메시지 ID는 필수 입니다") Long messageId) {
}
