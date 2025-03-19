package setting.SettingServer.entity.chat;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.StringUtils;
import setting.SettingServer.common.BaseTime;
import setting.SettingServer.entity.Member;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name; // 채팅방 이름

    @Column(unique = true, nullable = false, updatable = false)
    private String roomCode = UUID.randomUUID().toString();

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
        // ID는 룸 생성 시 자동생성
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

    public void updateCustomName(String newCustomName) {
        this.customName = newCustomName;
        if (StringUtils.hasText(newCustomName)) {
            this.name = newCustomName;
        } else {
            this.name = generateCurrentDefaultName();
        }
    }

    private String generateCurrentDefaultName() {
        if (this.roomType == ChatRoomType.DIRECT) {
            if (this.members.size() == 2) {
                Member member1 = this.members.get(0).getMember();
                Member member2 = this.members.get(1).getMember();
                return generateDefaultName(member1, member2);
            }
        }

        List<Member> membersList = this.members.stream()
                .map(ChatRoomMember::getMember)
                .collect(Collectors.toList());
        return generateDefaultGroupName(membersList);
    }

    private static String generateDefaultGroupName(List<Member> members) {
        int displayCount = Math.min(members.size(), 3);
        List<String> names = members.stream()
                .limit(displayCount)
                .map(Member::getName)
                .collect(Collectors.toList());

        String namesText = String.join(", ", names);

        if (members.size() > displayCount) {
            return String.format("%s 외 %d 명의 대화방", namesText, members.size() - displayCount);
        } else {
            return String.format("%s 님의 대화방", namesText);
        }
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

    public String getDisplayName() {
        return this.name;
    }

    // 회원별 맞춤 채팅방 이름 조회
    public String getDisplayNameForMember(Member member) {
        Optional<ChatRoomMember> chatRoomMember = this.members.stream()
                .filter(m -> m.getMember().equals(member))
                .findFirst();

        if (chatRoomMember.isPresent() && StringUtils.hasText(chatRoomMember.get().getNickname())) {
            return chatRoomMember.get().getNickname();
        }

        if (StringUtils.hasText(this.customName)) {
            return this.customName;
        }

        if (this.roomType == ChatRoomType.DIRECT && this.members.size() == 2) {
            ChatRoomMember otherMember = this.members.stream()
                    .filter(m -> m.getMember().equals(member))
                    .findFirst()
                    .orElse(null);

            if (otherMember != null) {
                return String.format("%s 님과의 대화", otherMember.getMember().getName());
            }
        }
        return this.name;
    }
}

