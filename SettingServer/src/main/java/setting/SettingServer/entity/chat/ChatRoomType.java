package setting.SettingServer.entity.chat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChatRoomType {
    DIRECT,
    GROUP
}
