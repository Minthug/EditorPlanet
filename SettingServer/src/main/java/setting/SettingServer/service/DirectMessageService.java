package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;
import setting.SettingServer.common.exception.UnauthorizedException;
import setting.SettingServer.dto.SendDirectMessageCommand;
import setting.SettingServer.dto.chat.ChatContactResponse;
import setting.SettingServer.dto.chat.ChatContractResponse;
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

    @Transactional(readOnly = true)
    public DirectMessage getMessage(Long messageId, Long userId) {
        DirectMessage message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을수 없습니다" + messageId));

        if (!message.getSender().getUserId().equals(userId) && !message.getReceiver().getUserId().equals(userId)) {
            throw new UnauthorizedException("해당 메시지에 접근할 권한이 없습니다");
        }

        if (message.isDeletedBySender() && message.getSender().getUserId().equals(userId)) {
            throw new EntityNotFoundException("삭제된 메시지 입니다");
        }

        if (message.isDeletedByReceiver() && message.getReceiver().getUserId().equals(userId)) {
            throw new EntityNotFoundException("삭제된 메시지 입니다");
        }

        return message;
    }

    @Transactional(readOnly = true)
    public Page<ChatContactResponse> getContractMessage(Long userId, Pageable pageable) {
        Page<Long> contactIds = directMessageRepository.findRecentContactIds(userId, pageable);

        return contactIds.map(contactId -> {
            Member contact = memberRepository.findById(contactId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다" + contactId));

            DirectMessage latestMessage = directMessageRepository.findLatestMessage(userId, contactId, PageRequest.of(0, 1))
                    .stream().findFirst().orElse(null);

            long unreadCount = directMessageRepository.countByReceiverIdAndSenderIdAndIsReadFalseAndIsDeletedByReceiverFalse(userId, contactId);

            return ChatContactResponse.builder()
                    .contactId(contactId)
                    .contactName(contact.getName())
                    .profileImage(contact.getImageUrl())
                    .latestMessage(latestMessage != null ? latestMessage.getContent() : null)
                    .latestMessageTime(latestMessage != null ? latestMessage.getSentAt() : null)
                    .unreadCount(unreadCount)
                    .build();
        });
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

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        log.info("메시지 삭제 요청: 메시지 ID={}, 유저 ID={}", messageId, userId);

        DirectMessage message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다" + messageId));

        boolean isSender = message.getSender().getUserId().equals(userId);
        boolean isReceiver = message.getReceiver().getUserId().equals(userId);

        if (!isSender && !isReceiver) {
            log.warn("메시지 삭제 권한 없음: 메시지 ID={}, 요청자 ID={}", messageId, userId);
            throw new UnauthorizedException("해당 메시지 삭제 권한이 없습니다");
        }

        if (isSender) {
            message.markDeletedBySender();
        }

        if (isReceiver) {
            message.markDeletedByReceiver();
        }

        if (message.isDeletedBySender() && message.isDeletedByReceiver()) {
            directMessageRepository.delete(message);
            log.info("메시지 영구 삭제 완료: 메시지 ID={}", messageId);
        } else {
            log.info("메시지 삭제 표시 완료: 메시지 ID={}, 발신자 삭제={}, 수신자 삭제={}", messageId,
                    message.isDeletedBySender(), message.isDeletedByReceiver());
        }
    }

    @Transactional(readOnly = true)
    public long countUnreadMessage(Long receiverId) {
        log.debug("안읽은 메시지 조회: 수신자 ID={}", receiverId);

        if (!memberRepository.existsById(receiverId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다" + receiverId);
        }

        return directMessageRepository.countByReceiverIdAndIsReadFalseAndIsDeletedByReceiverFalse(receiverId);
    }
}