package setting.SettingServer.entity.chat;

import jakarta.persistence.*;
import lombok.*;
import setting.SettingServer.common.BaseTime;
import setting.SettingServer.entity.Member;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column
    private String name; // 채팅방 이름

    @Column
    private String customName; // 사용자 지정 이름 (null 시 자동 생성)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType roomType;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<ChatRoomMember> members = new ArrayList<>();

    // 일대일 채팅방 생성
    public static ChatRoom createDirectChat(Member user1, Member user2) {
        ChatRoom room = new ChatRoom();
        room.id = UUID.randomUUID().toString();
        room.roomType = ChatRoomType.DIRECT;

        room.name = generateDefaultName(user1, user2);

        room.addMember(user1, ChatRoomMemberRole.MEMBER);
        room.addMember(user2, ChatRoomMemberRole.MEMBER);
    }

    private static String generateDefaultName(Member user1, Member user2) {
    }


    public void addMember(Member member, ChatRoomMemberRole role) {
        boolean alreadyExists = this.members.stream()
                .anyMatch(m -> m.getMember().equals(member) && m.isActive());

        if (alreadyExists) {
            throw new IllegalStateException("이미 채팅방에 존재하는 멤버 입니다: " + member.getId());
        }

        ChatRoomMember chatRoomMember = ChatRoomMember.builder()
                .chatRoom(this)
                .member(member)
                .role(role)
                .build();

        this.members.add(chatRoomMember);
    }
}

