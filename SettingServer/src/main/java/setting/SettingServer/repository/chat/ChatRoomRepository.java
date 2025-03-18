package setting.SettingServer.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.entity.chat.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m1 JOIN cr.members m2 " +
            "WHERE cr.roomType = 'DIRECT' " +
            "AND m1.member.id = :userId1 AND m2.member.id = :userId2 " +
            "AND m1.status = 'ACTIVE' AND m2.status = 'ACTIVE'")
    Optional<ChatRoom> findDirectChatRoom(@Param("userId1") String userId1, @Param("userId2") String userId2);

}