package setting.SettingServer.service.chat;

import setting.SettingServer.dto.chat.ChatMessageDto;
import setting.SettingServer.entity.chat.ChatMessage;

public interface ChatMessageService {

    public ChatMessage createChatMessage(ChatMessageDto chatMessageDto);
}
