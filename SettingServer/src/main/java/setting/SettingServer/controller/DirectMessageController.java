package setting.SettingServer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import setting.SettingServer.service.DirectMessageService;

@Controller
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;


}
