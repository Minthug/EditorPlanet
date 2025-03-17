package setting.SettingServer.dto.chat;

import lombok.Data;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


/**
 * 채팅방 목록 응답 DTO
 * @param rooms
 * @param currentPage
 * @param totalPages
 * @param totalElements
 * @param unreadCounts 채팅방 ID별 안 읽은 메시지 수
 */
public record ChatRoomListDto(List<ChatRoomDto> rooms, int currentPage, int totalPages, long totalElements,
                              Map<String, Long> unreadCounts) {

}
