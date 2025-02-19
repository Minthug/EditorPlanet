package setting.SettingServer.service.request;

public record DirectMessageRequest(Long senderId, Long receiverId, String content) {
}
