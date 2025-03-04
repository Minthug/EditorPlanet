package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import setting.SettingServer.service.DirectMessageService;
import setting.SettingServer.service.request.SendDirectMessageRequest;

@Controller
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /***
     * [클라이언트] → Request → [컨트롤러] → Command → [서비스] → 엔티티/도메인 처리 → [서비스] → Response → [컨트롤러] → [클라이언트]
     */
    @PostMapping("/direct-message")
    public ResponseEntity<Void> createDirectMessage(@Valid @RequestBody SendDirectMessageRequest request) {


    }
}
