package setting.SettingServer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.dto.chat.ChatRoomInfoResponse;
import setting.SettingServer.dto.chat.ChatRoomListResponse;
import setting.SettingServer.dto.chat.CreatedChatRoomRequest;
import setting.SettingServer.dto.chat.CreatedChatRoomResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/personal")
    public CreatedChatRoomResponse createPersonalChatRoom(@RequestBody CreatedChatRoomRequest request) {
        return chatRoomService.createChatRoomForPersonal(request);
    }

    @GetMapping("/message")
    public ChatRoomInfoResponse chatRoomInfo(@RequestParam int page, @RequestParam int size, @RequestParam String roomId) {
        return chatRoomService.chatRoomInfo(roomId, page, size);
    }

    @GetMapping("/list")
    public ChatRoomListResponse getChatRoomList(@RequestParam int page, @RequestParam int size) {
        return chatRoomService.getChatRoomList(page, size);
    }

}

