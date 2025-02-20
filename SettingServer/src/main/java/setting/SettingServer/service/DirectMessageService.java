package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;
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
    public Long sendMessage(DirectMessageRequest request) {
        Member sender = memberRepository.findById(request.senderId())
                .orElseThrow(() -> new EntityNotFoundException("발신자를 찾을 수 없습니다"));

        Member receiver = memberRepository.findById(request.receiverId())
                .orElseThrow(() -> new EntityNotFoundException("수신자를 찾을 수 없습니다"));

        DirectMessage message = DirectMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .isRead(false)
                .build();
        return directMessageRepository.save(message).getId();
    }

    public List<DirectMessageResponse> getReceivedMessage(Long memberId) {
        return directMessageRepository.findByReceiverId(memberId)
                .stream()
                .map(DirectMessageResponse::from)
                .collect(Collectors.toList());
    }
}
