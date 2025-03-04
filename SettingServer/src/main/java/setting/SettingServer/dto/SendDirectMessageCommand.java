package setting.SettingServer.dto;

import setting.SettingServer.entity.MessageAttachment;
import setting.SettingServer.service.request.SendDirectMessageRequest;

import java.time.LocalDateTime;
import java.util.List;

public record SendDirectMessageCommand(Long senderId, Long receiverId, String content,
                                       List<MessageAttachment> attachments, LocalDateTime sendAt) {

    public static SendDirectMessageCommand from(SendDirectMessageRequest request, Long currentUserId) {
        return new SendDirectMessageCommand(
                currentUserId,
                request.receiverId(),
                request.content(),
                request.attachments(),
                LocalDateTime.now()
        );
    }
}
