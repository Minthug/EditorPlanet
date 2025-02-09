package setting.SettingServer.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.chat.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findListsByRoomId(String roomId, Pageable pageable);

    void deleteByRoomIdIn(List<String> roomIds); //리스트에 포함된 모든 ChatMessage 삭제



}
