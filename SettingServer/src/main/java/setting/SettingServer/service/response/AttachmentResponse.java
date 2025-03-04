package setting.SettingServer.service.response;

import setting.SettingServer.entity.MessageAttachment;

public record AttachmentResponse(
        Long id,
        Long fileId,
        String fileName,
        Long fileSize,
        String contentType
) {
    public static AttachmentResponse from(MessageAttachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getFileId(),
                attachment.getFileName(),
                attachment.getFileSize(),
                attachment.getContentType()
        );
    }
}