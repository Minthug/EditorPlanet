package setting.SettingServer.controller;

import com.sun.security.auth.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import setting.SettingServer.common.CustomUserDetails;
import setting.SettingServer.dto.SendDirectMessageCommand;
import setting.SettingServer.service.DirectMessageService;
import setting.SettingServer.service.request.SendDirectMessageRequest;

import java.net.URI;

@RestController
@RequestMapping("/v1/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /***
     * [클라이언트] → Request → [컨트롤러] → Command → [서비스] → 엔티티/도메인 처리 → [서비스] → Response → [컨트롤러] → [클라이언트]
     */
    @PostMapping("/direct-message")
    public ResponseEntity<Void> createDirectMessage(@Valid @RequestBody SendDirectMessageRequest request) {

        Long currentUserId = getCurrentUserId();

        SendDirectMessageCommand command = SendDirectMessageCommand.from(request, currentUserId);

        Long messageId = directMessageService.sendMessage(command);

        URI location = URI.create("/v1/direct-messages/" + messageId);
        return ResponseEntity.created(location).build();
    }


    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return Long.parseLong(oauth2User.getAttribute("sub"));
//        } else if (auth instanceof JwtAuthenticationToken) {
//            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) auth;
//            return Long.parseLong(jwtAuth.getToken().getClaim("user_id").toString());
        }

        throw new IllegalStateException("지원되지 않는 인증 방식입니다");
    }
}
