package setting.SettingServer.dto.chat;

import java.util.List;

/**
 * 채팅 메시지 목록 응답 DTO
 * @param messages
 * @param currentPage
 * @param totalPages
 * @param totalElements
 */
public record ChatMessageListDto(List<ChatMessageDto> messages, int currentPage, int totalPages, long totalElements) {
}
