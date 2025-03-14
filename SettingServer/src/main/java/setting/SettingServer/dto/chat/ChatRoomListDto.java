package setting.SettingServer.dto.chat;

import lombok.Data;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;

public record ChatRoomListDto() {
//    private String chatRoomId;
//    private ChatMessage lastChatMesg;
//    private String guestId;
//
//    public ChatRoomList(ChatRoom chatRoom, String userId) {
//        this.chatRoomId = chatRoom.getId();
//        this.lastChatMesg = chatRoom.getLatestChatMessage();
//        for (Member member : chatRoom.getChatRoomMembers()) {
//            if (!member.getUserId().equals(userId)) {
//                this.guestId = userId;
//            }
//        }
//    }
}
