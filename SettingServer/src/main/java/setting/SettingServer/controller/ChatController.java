package setting.SettingServer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.service.chat.ChatRoomService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class ChatController {


    private final ChatRoomService chatRoomService;
}

