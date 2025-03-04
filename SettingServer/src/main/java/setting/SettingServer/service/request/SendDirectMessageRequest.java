package setting.SettingServer.service.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import setting.SettingServer.entity.MessageAttachment;

import java.util.List;

public record SendDirectMessageRequest(@NotNull(message = "수신자 ID는 필수 입력 사항 입니다") Long receiverId,
                                       @NotBlank(message = "메시지 내용은 필수 입력 사항 입니다")
                                       @Size(max = 1000, message = "메시지는 1000자까지 입력 가능합니다")
                                       String content,
                                       @Valid
                                       List<MessageAttachment> attachments) {
}
