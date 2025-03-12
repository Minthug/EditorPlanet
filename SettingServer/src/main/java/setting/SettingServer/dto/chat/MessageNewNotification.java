package setting.SettingServer.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageNewNotification {

    private Long messageId;
    private Long senderId;
    private String previewContent;
    private LocalDateTime timestamp;

}
