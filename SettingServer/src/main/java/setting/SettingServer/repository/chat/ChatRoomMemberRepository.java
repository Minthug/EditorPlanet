package setting.SettingServer.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatRoom;
import setting.SettingServer.entity.chat.ChatRoomMember;
import setting.SettingServer.entity.chat.ChatRoomMemberStatus;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    Optional<ChatRoomMember> findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    List<ChatRoomMember> findByMemberAndStatus(Member member, ChatRoomMemberStatus status);

    List<ChatRoomMember> findByChatRoomAndStatusOrderByCreatedAtAsc(ChatRoom chatRoom, ChatRoomMemberStatus status);

    @Query("SELECT crm FROM ChatRoomMember crm " +
            "WHERE crm.member = :member AND crm.status = :status " +
            "ORDER BY (SELECT MAX(cm.createdAt) FROM ChatMessage cm WHERE cm.chatRoom = crm.chatRoom) DESC ")
    Page<ChatRoomMember> findByMemberAndStatusOrderByLastMessageTimestampDesc(Member member, ChatRoomMemberStatus status, Pageable pageable);

    long countByChatRoomAndStatus(ChatRoom chatRoom, ChatRoomMemberStatus status);
}
