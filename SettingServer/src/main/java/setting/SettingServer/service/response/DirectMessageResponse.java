package setting.SettingServer.service.response;

import setting.SettingServer.entity.DirectMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record DirectMessageResponse (Long id, Long senderId, String senderName, Long receiverId, String content, List<AttachmentResponse> attachments, LocalDateTime sentAt, boolean isRead) {

    public static DirectMessageResponse from(DirectMessage message) {
        List<AttachmentResponse> attachmentResponses = message.getAttachments().stream()
                .map(AttachmentResponse::from)
                .collect(Collectors.toList());

        return new DirectMessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getSenderName(),
                message.getReceiverId(),
                message.getContent(),
                attachmentResponses,
                message.getSentAt(),
                message.isRead()
        );

    }
}
