package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public List<DirectMessageResponse> getReceivedMessages(Long memberId, Pageable pageable) {

        log.debug("받은 메시지 조회: 회원 ID={}, 페이지 크기={}, 크기={}", memberId, pageable.getPageNumber(), pageable.getPageSize());

        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.(ID: " + memberId + ")");
        }

        return directMessageRepository.findBy)
                .collect(Collectors.toList());
    }

    public List<DirectMessageResponse> getSentMessages(Long memberId) {
        return directMessageRepository.findBySenderId(memberId)
                .stream()
                .map(DirectMessageResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long messageId, Long memberId) {
        DirectMessage message = directMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메세지를 찾을 수 없습니다"));

        if (!message.getReceiver().getUserId().equals(memberId)) {
            throw new UnauthorizedException("읽음 처리 권한이 없습니다");
        }

        message.markAsRead();
    }
}
