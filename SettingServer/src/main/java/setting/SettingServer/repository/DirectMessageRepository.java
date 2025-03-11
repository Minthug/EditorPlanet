package setting.SettingServer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.dto.chat.MessagePreviewResponse;
import setting.SettingServer.entity.DirectMessage;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    Page<DirectMessage> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    Page<DirectMessage> findBySenderIdOrderByCreatedAtDesc(Long senderId, Pageable pageable);

    List<DirectMessage> findByReceiverId(Long memberId);

    List<DirectMessage> findBySenderId(Long memberId);

    // 페이징 적용된 조회 메서드
    Page<DirectMessage> findByReceiverIdAndIsDeletedByReceiverFalseOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    Page<DirectMessage> findBySenderIdAndIsDeletedBySenderFalseOrderByCreatedAtDesc(Long senderId, Pageable pageable);


    @Query("SELECT dm FROM DirectMessage dm " +
            "WHERE (dm.sender.id = :userId1 AND dm.receiver.id = :userId2) " +
            "OR (dm.sender.id = :userId2 AND dm.receiver.id = :userId1) " +
            "ORDER BY dm.sentAt")
    Page<DirectMessage> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);

    // 안 읽은 메시지 수 조회
    long countByReceiverIdAndIsReadFalseAndIsDeletedByReceiverFalse(Long receiverId);

    // 특정 발신자로부터 받은 안 읽은 메시지 수 조회
    long countByReceiverIdAndSenderIdAndIsReadFalseAndIsDeletedByReceiverFalse(Long receiverId, Long senderId);

    // 모든 메시지 일괄 읽음 처리
    @Modifying
    @Query("UPDATE DirectMessage dm SET dm.isRead = true " +
            "WHERE dm.receiver.id = :receiverId AND dm.isRead = false AND dm.isDeletedByReceiver = false")
    int markAllAsRead(@Param("receiverId") Long receiverId);

    // 특정 발신자의 메시지 일괄 읽음 처리
    @Modifying
    @Query("UPDATE DirectMessage dm SET dm.isRead = true " +
            "WHERE dm.receiver.id = :receiverId AND dm.sender.id = :senderId " +
            "AND dm.isRead = false AND dm.isDeletedByReceiver = false")
    int markAllAsReadBySender(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);

    // 최근 대화 상대 목록 조회 (가장 최근 메시지 기준)
    @Query(value = "SELECT DISTINCT CASE " +
            "WHEN dm.sender_id = :userId THEN dm.receiver_id " +
            "ELSE dm.sender_id END AS contact_id " +
            "FROM direct_message dm " +
            "WHERE ((dm.sender_id = :userId AND dm.is_deleted_by_sender = false) " +
            "OR (dm.receiver_id = :userId AND dm.is_deleted_by_receiver = false)) " +
            "GROUP BY contact_id " +
            "ORDER BY MAX(dm.created_at) DESC",
            nativeQuery = true)
    Page<Long> findRecentContactIds(@Param("userId") Long userId, Pageable pageable);


        @Query("SELECT new setting.SettingServer.dto.chat.MessagePreviewResponse(" +
                "dm.id, dm.content, dm.sentAt, dm.isRead) " +
                "FROM DirectMessage dm " +
                "WHERE ((dm.sender.id = :userId1 AND dm.receiver.id = :userId2 AND dm.isDeletedBySender = false) " +
                "OR (dm.sender.id = :userId2 AND dm.receiver.id = :userId1 AND dm.isDeletedByReceiver = false)) " +
                "ORDER BY dm.sentAt DESC")
        List<DirectMessage> findLatestMessage(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);
}
