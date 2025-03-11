package setting.SettingServer.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatContactResponse {
    private Long contactId;         // 대화 상대 ID
    private String contactName;     // 대화 상대 이름
    private String profileImage;    // 프로필 이미지 URL (옵션)

    private String latestMessage;   // 가장 최근 메시지 내용
    private LocalDateTime latestMessageTime; // 가장 최근 메시지 시간
    private long unreadCount;       // 안 읽은 메시지 수
}