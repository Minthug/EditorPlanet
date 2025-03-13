package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.common.CustomUserDetails;
import setting.SettingServer.common.oauth.JwtAuthenticationToken;
import setting.SettingServer.dto.SendDirectMessageCommand;
import setting.SettingServer.dto.chat.MessageNewNotification;
import setting.SettingServer.entity.DirectMessage;
import setting.SettingServer.service.DirectMessageService;
import setting.SettingServer.service.request.SendDirectMessageRequest;
import setting.SettingServer.service.response.DirectMessageResponse;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/v1/direct-messages")
@RequiredArgsConstructor
@Slf4j
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

        notifyNewMessage(request.receiverId(), messageId, currentUserId, request.content());

        URI location = URI.create("/v1/direct-messages/" + messageId);
        return ResponseEntity.created(location).build();
    }


    // 메시지 단일 조회
    @GetMapping("/{messageId}")
    public ResponseEntity<DirectMessageResponse> getMessage(@PathVariable Long messageId) {
        Long currentUserId = getCurrentUserId();
        log.debug("메시지 조회 요청: 메시지 ID={}, 사용자 ID={}", messageId, currentUserId);

        DirectMessage message = directMessageService.getMessage(messageId, currentUserId);

        if (message.getReceiver().getUserId().equals(currentUserId) && !message.isRead()) {
            directMessageService.markAsRead(messageId, currentUserId);
        }

        return ResponseEntity.ok(DirectMessageResponse.from(message));
    }

    // 보낸 메시지 목록 조회
    @GetMapping("/sent")
    public ResponseEntity<Page<DirectMessageResponse>> getSentMessage(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "20") int size) {

        Long currentUserId = getCurrentUserId();
        log.debug("보낸 메시지 목록 요청: 사용자 ID={}, 페이지={}, 크기={}", currentUserId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<DirectMessageResponse> messages = directMessageService.getSentMessages(currentUserId, pageable);

        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversation/{otherUserId}")
    public ResponseEntity<Page<DirectMessageResponse>> getConversation(@PathVariable Long otherUserId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size) {

        Long currentUserId = getCurrentUserId();
        log.debug("대화 내용 요청: 사용자 ID={}, 상대방 ID={}, 페이지={}, 크기={}", currentUserId, otherUserId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<DirectMessageResponse> conversation = directMessageService.getConversation(currentUserId, otherUserId, pageable);

        return ResponseEntity.ok(conversation);
    }

    // 메시지 읽음 처리
    @PatchMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(@PathVariable Long messageId) {
        Long currentUserId = getCurrentUserId();
        log.debug("메시지 읽음 처리 요청: 메시지 ID={}, 사용자 ID={}", messageId, currentUserId);

        directMessageService.markAsRead(messageId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // 메시지 일괄 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllMessageAsRead(@RequestParam(required = false) Long senderId) {
        Long currentUserId = getCurrentUserId();
        log.info("메시지 일괄 읽음 처리 요청: 사용자 ID={}, 발신자 ID={}", currentUserId, senderId);

        int updateCount = directMessageService.markAllRead(currentUserId, senderId);
        Map<String, Integer> response = Collections.singletonMap("UpdatedCount", updateCount);
        return ResponseEntity.ok(response);
    }

    // 메시지 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long messageId) {
        Long currentUserId = getCurrentUserId();
        log.info("메시지 삭제 요청: 메시지 ID={}, 사용자 ID={}", messageId, currentUserId);

        directMessageService.deleteMessage(messageId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    // 안읽은 메시지 수 조회
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount() {
        Long currentUserId = getCurrentUserId();
        log.debug("안읽은 메시지 수 처리 요청: 사용자 ID={}", currentUserId);

        long unreadCount = directMessageService.countUnreadMessage(currentUserId);
        Map<String, Long> response = Collections.singletonMap("UnreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }



    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getId();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return Long.parseLong(oauth2User.getAttribute("sub"));
        }

        throw new IllegalStateException("지원되지 않는 인증 방식입니다");
    }


    private void notifyNewMessage(Long receiverId, Long messageId, Long senderId, String content) {
        try {
            MessageNewNotification notification = new MessageNewNotification(
                    messageId, senderId, content, LocalDateTime.now());

            simpMessagingTemplate.convertAndSendToUser(String.valueOf(receiverId),
                    "/queue/messages",
                    notification
                    );

            log.debug("실시간 메시지 알림 전송: 수신자={}, 발신자={}", receiverId, senderId);
        } catch (Exception e) {
            log.warn("메시지 알림 전송 실패: {}", e);
        }
    }
}
