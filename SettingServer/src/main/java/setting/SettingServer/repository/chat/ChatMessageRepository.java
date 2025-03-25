package setting.SettingServer.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;
import setting.SettingServer.entity.chat.MessageType;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom, Pageable pageable);

    Optional<ChatMessage> findTopByChatRoomOrderByCreatedAtDesc(ChatRoom chatRoom);

    long countByChatRoomAndMessageTypeNot(ChatRoom chatRoom, MessageType messageType);

    long countByChatRoomAndIdGreaterThanAndMessageTypeNot(ChatRoom chatRoom, Long messageId, MessageType messageType);

    long countByChatRoomAndIdLessThanEqualAndMessageTypeNot(ChatRoom chatRoom, Long messageId, MessageType messageType);

    long countByChatRoomAndIdBetweenAndMessageTypeNot(ChatRoom chatRoom, Long startId, Long endId, MessageType messageType);

}
