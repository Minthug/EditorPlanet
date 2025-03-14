package setting.SettingServer.entity.chat;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;
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

        return room;
    }

    // 그룹 채팅방 생성
    public static ChatRoom createGroupChat(List<Member> members, Member creator, String customName) {
        if (members.size() < 2) {
            throw new IllegalArgumentException("그룹 채팅방은 최소 2인 이상의 멤버가 필요합니다");
        }

        ChatRoom room = new ChatRoom();
        room.id = UUID.randomUUID().toString();
        room.roomType = ChatRoomType.GROUP;

        if (StringUtils.hasText(customName)) {
            room.customName = customName;
            room.name = customName;
        } else {
            room.name = generateDefaultGroupName(members);
        }

        room.addMember(creator, ChatRoomMemberRole.OWNER);

        for (Member member : members) {
            if (!member.equals(creator)) {
                room.addMember(member, ChatRoomMemberRole.MEMBER);
            }
        }

        return room;
    }

    private static String generateDefaultGroupName(List<Member> members) {
        return null;
    }

    private static String generateDefaultName(Member user1, Member user2) {
        return String.format("%s, %s 님의 대화방", user1.getName(), user2.getName());
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

