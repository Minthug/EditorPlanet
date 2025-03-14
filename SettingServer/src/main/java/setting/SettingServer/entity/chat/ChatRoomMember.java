package setting.SettingServer.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;
import setting.SettingServer.common.BaseTime;
import setting.SettingServer.entity.Member;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Table(name = "chat_room_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomMemberRole role;

    private Long lastReadMessageId;

    @Column(nullable = false)
    private boolean notificationsEnabled = true;

    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomMemberStatus status = ChatRoomMemberStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    private LocalDateTime leftAt;

    @Builder
    public ChatRoomMember(ChatRoom chatRoom, Member member, ChatRoomMemberRole role, LocalDateTime joinedAt) {
        this.chatRoom = chatRoom;
        this.member = member;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    // 마지막 읽은 메시지 ID 업데이트
    public void updateLastReadMessageId(Long messageId) {
        if (messageId == null || (this.lastReadMessageId != null && messageId < this.lastReadMessageId)) {
            throw new IllegalArgumentException("유효하지 않은 메시지 ID 입니다");
        }
        this.lastReadMessageId = messageId;
    }

    // 알림 설정 토글
    public void toggleNotifications() {
        this.notificationsEnabled = !this.notificationsEnabled;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateRole(ChatRoomMemberRole role) {
        this.role = role;
    }

    public void leave() {
        this.status = ChatRoomMemberStatus.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    public void rejoin() {
        if (this.status == ChatRoomMemberStatus.LEFT) {
            this.status = ChatRoomMemberStatus.ACTIVE;
            this.leftAt = null;
        }
    }

    public void kick() {
        this.status = ChatRoomMemberStatus.KICKED;
        this.leftAt = LocalDateTime.now();
    }

    public void block() {
        this.status = ChatRoomMemberStatus.BLOCKED;
    }

    public void unblock() {
        if (this.status == ChatRoomMemberStatus.BLOCKED) {
            this.status = ChatRoomMemberStatus.ACTIVE;
        }
    }

    public boolean isActive() {
        return this.status == ChatRoomMemberStatus.ACTIVE;
    }

    // equals 및 hashCode 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoomMember that = (ChatRoomMember) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
