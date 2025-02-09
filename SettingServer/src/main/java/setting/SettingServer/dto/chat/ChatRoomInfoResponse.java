package setting.SettingServer.dto.chat;

import lombok.Data;
import lombok.Getter;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class ChatRoomInfoResponse {

    private String chatRoomId;
    private ChatMessage lastChatMesg;
    private Set<ChatUserInfoDto> chatRoomMembers;
    private List<ChatMessageInfo> latestChatMessages;
    private LocalDateTime createdAt;

    public ChatRoomInfoResponse(ChatRoom chatRoom) {
        this.chatRoomId = chatRoom.getId();
        this.lastChatMesg = chatRoom.getLatestChatMessage();
        this.chatRoomMembers = new HashSet<>();
        for (Member member : chatRoom.getChatRoomMembers()) {
            chatRoomMembers.add(new ChatUserInfoDto(member));
        }
        this.createdAt = chatRoom.getCreatedAt();
    }
}
