package setting.SettingServer.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 채팅방 초대 멤버 요청 DTO
 * @param inviteeId
 */
public record InviteMemberRequest(@NotNull(message = "초대자 ID는 필수 입니다") Long inviterId,
                                  @NotBlank(message = "초대할 사용자 ID는 필수 입니다") Long inviteeId) {
}
