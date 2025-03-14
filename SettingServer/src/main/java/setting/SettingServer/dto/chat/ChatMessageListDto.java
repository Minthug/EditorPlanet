package setting.SettingServer.dto.chat;

import java.util.List;

public record ChatMessageListDto(List<ChatMessageDto> messages, int currentPage, int totalPages, long totalElements) {
}
