package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.DirectMessage;
import setting.SettingServer.entity.MessageAttachment;

import java.time.LocalDateTime;
import java.util.List;

public record DirectMessageResponse(Long id, String content, Long senderId, String senderName,
                                    List<MessageAttachment> attachments,
                                    boolean isRead, LocalDateTime sentAt) {

    public static DirectMessageResponse from(DirectMessage message) {
        return new DirectMessageResponse(
                message.getId(),
                message.getSender(),
                message.
        )

    }
}
