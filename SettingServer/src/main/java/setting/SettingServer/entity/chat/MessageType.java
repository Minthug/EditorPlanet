package setting.SettingServer.entity.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    CHAT,
    JOIN,
    LEAVE,
    SERVER,
    ENTER,
    PRIVATE
}