package setting.SettingServer.repository.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.entity.chat.ChatRoom;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

}