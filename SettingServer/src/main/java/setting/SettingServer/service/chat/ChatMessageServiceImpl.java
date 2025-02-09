package setting.SettingServer.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import setting.SettingServer.dto.chat.ChatMessageDto;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;
import setting.SettingServer.repository.chat.ChatRoomRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService{

    private final ChatRoomRepository chatRoomRepository;

    @Override
    public ChatMessage createChatMessage(ChatMessageDto chatMessageDto) {

        ChatRoom chatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Chat Room not Found"));

        boolean exists = chatRoom.getChatRoomMembers().stream()
                .anyMatch(member -> member.getUserId().equals(chatMessageDto.getAuthorId()));

        if (!exists) {
            log.error("User not found in chat room: {}", chatMessageDto.getAuthorId());
            return null;
        }

        ChatMessage chatMessage = chatMessageDto.toEntity();
        chatRoom.setLatestChatMessage(chatMessage);
        chatRoomRepository.save(chatRoom);

        return chatMessage;
    }
}
