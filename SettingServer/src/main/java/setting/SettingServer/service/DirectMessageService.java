package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;
import setting.SettingServer.common.exception.UnauthorizedException;
import setting.SettingServer.dto.SendDirectMessageCommand;
import setting.SettingServer.entity.DirectMessage;
import setting.SettingServer.entity.Member;
import setting.SettingServer.repository.DirectMessageRepository;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.service.request.DirectMessageRequest;
import setting.SettingServer.service.response.DirectMessageResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long sendMessage(SendDirectMessageCommand request) {
        Member sender = memberRepository.findById(request.senderId())
                .orElseThrow(() -> new EntityNotFoundException("발신자를 찾을 수 없습니다"));

        Member receiver = memberRepository.findById(request.receiverId())
                .orElseThrow(() -> new EntityNotFoundException("수신자를 찾을 수 없습니다"));

        if (sender.getUserId().equals(receiver.getId())) {
            throw new IllegalArgumentException("자기 자신에게 메시지를 보낼수 없습니다");
        }

        DirectMessage message = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .isRead(false)
                .build();

        DirectMessage savedMessage = directMessageRepository.save(message);
        log.info("메시지 전송 완료: 메시지 ID={}", savedMessage.getId());

        return savedMessage.getId();
    }

    public Page<DirectMessageResponse> getReceivedMessages(Long memberId, Pageable pageable) {

        log.debug("받은 메시지 조회: 회원 ID={}, 페이지 크기={}, 크기={}", memberId, pageable.getPageNumber(), pageable.getPageSize());

        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.(ID: " + memberId + ")");
        }

        return directMessageRepository.findByReceiverIdOrderByCreatedAtDesc(memberId, pageable)
                .map(DirectMessageResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<DirectMessageResponse> getSentMessages(Long memberId, Pageable pageable) {

        log.debug("보낸 메시지 조회: 회원 ID={}, 페이지={}, 크기={}", memberId, pageable.getPageNumber(), pageable.getPageSize());

        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.(ID: " + memberId + ")");

            }

        return directMessageRepository.findBySenderIdOrderByCreatedAtDesc(memberId, pageable)
                .map(DirectMessageResponse::from);
    }

    @Transactional
    public Page<DirectMessageResponse> getConversation(Long userId, Long otherUserId, Pageable pageable) {
        log.debug("대화 내역 조회: 사용자 ID={}, 상대방 ID={}", userId, otherUserId);

        if (!memberRepository.existsById(userId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다(ID: " + userId + ")");
        }

        if (!memberRepository.existsById(otherUserId)) {
            throw new EntityNotFoundException("상대 회원을 찾을 수 없습니다(ID: " + otherUserId + ")");
        }

        return directMessageRepository.findConversation(userId, otherUserId, pageable)
                .map(DirectMessageResponse::from);
    }

    @Transactional
    public void markAsRead(Long messageId, Long memberId) {
        log.debug("메시지 읽음 처리: 메시지 ID={}, 회원 ID={}", messageId, memberId);

        DirectMessage message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메세지를 찾을 수 없습니다"));

        if (!message.getReceiver().getUserId().equals(memberId)) {
            throw new UnauthorizedException("읽음 처리 권한이 없습니다");
        }

        if (!message.isRead()) {
            message.markAsRead();
            log.info("메시지 읽음 처리 완료: 메시지 ID={}", messageId);
        } else {
            log.debug("이미 읽은 메시지: 메시지 ID={}", messageId);
        }
    }

    @Transactional
    public int markAllRead(Long receiverId, Long senderId) {
        log.info("메시지 읽음 처리: 수신자 ID={}, 발신자 ID={}", receiverId, senderId);

        if (!memberRepository.existsById(receiverId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다. ID: " + receiverId);
        }

        if (senderId != null && !memberRepository.existsById(senderId)) {
            throw new EntityNotFoundException("발신자를 찾을 수 없습니다. ID: " + senderId);
        }

        int updatedCount;
        if (senderId != null) {
            updatedCount = directMessageRepository.markAllAsReadBySender(receiverId, senderId);
        } else {
            updatedCount = directMessageRepository.markAllAsRead(receiverId);
        }

        log.info("일괄 읽음 처리: {} 개의 메시지 업데이트 됨", updatedCount);
        return updatedCount;
    }
}
