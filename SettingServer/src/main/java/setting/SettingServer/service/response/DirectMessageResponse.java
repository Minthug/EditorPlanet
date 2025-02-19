package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.DirectMessage;

import java.time.LocalDateTime;

public record DirectMessageResponse(Long id, String content, MemberResponse sender, MemberResponse receiver,
                                    boolean isRead, LocalDateTime sentAt) {

    public static DirectMessageResponse from(DirectMessage directMessage) {
        return new DirectMessageResponse(
                directMessage.getId(),
                directMessage.getContent(),
                MemberResponse.of(directMessage.getSender()),
                MemberResponse.of(directMessage.getReceiver()),
                directMessage.isRead(),
                directMessage.getSendAt()
        );
    }
}
