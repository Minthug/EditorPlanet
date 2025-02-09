package setting.SettingServer.common.chat;

public class ChatRoomAccessDeniedException extends RuntimeException {
    public ChatRoomAccessDeniedException(String message) {
        super(message);
    }

    public ChatRoomAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
