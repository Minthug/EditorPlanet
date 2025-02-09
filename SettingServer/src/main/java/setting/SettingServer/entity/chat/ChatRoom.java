package setting.SettingServer.entity.chat;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import setting.SettingServer.entity.Member;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DynamicUpdate
@EntityListeners(value = {AuditingEntityListener.class})
public class ChatRoom {

    @EqualsAndHashCode.Include
    @Id
    private String id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private ChatMessage latestChatMessage;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "ChatRoom_Members",
            joinColumns = @JoinColumn(name = "chatRoomId"),
            inverseJoinColumns = @JoinColumn(name = "memberId"))
    private Set<Member> chatRoomMembers = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    public static ChatRoom create() {
        ChatRoom room = new ChatRoom();

        room.setId(UUID.randomUUID().toString());
        return room;
    }

    public void addMembers(Member roomMaker, Member guest) {
        this.chatRoomMembers.add(roomMaker);
        this.chatRoomMembers.add(guest);
    }
}
