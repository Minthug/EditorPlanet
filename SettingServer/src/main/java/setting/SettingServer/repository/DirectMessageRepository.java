package setting.SettingServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.entity.DirectMessage;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findBySenderIdOrderBySentAtDesc(Long senderId);

    List<DirectMessage> findByReceiverIdOrderBySendAtDesc(Long receiverId);

    @Query("SELECT dm FROM DirectMessage dm " +
            "WHERE (dm.sender.id = :userId1 AND dm.receiver.id = :userId2) " +
            "OR (dm.sender.id = :userId2 AND dm.receiver.id = :userId1) " +
            "ORDER BY dm.sendAt")
    List<DirectMessage> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
