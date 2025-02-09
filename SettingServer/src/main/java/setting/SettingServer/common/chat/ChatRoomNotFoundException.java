package setting.SettingServer.common.chat;

public class ChatRoomNotFoundException extends RuntimeException {

    public ChatRoomNotFoundException(String message) {
        super(message);
    }

    public ChatRoomNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
