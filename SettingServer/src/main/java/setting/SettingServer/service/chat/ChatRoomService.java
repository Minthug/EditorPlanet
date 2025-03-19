package setting.SettingServer.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.UnauthorizedException;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.dto.chat.ChatMessageDto;
import setting.SettingServer.dto.chat.ChatRoomDto;
import setting.SettingServer.dto.chat.ChatRoomMemberDto;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;
import setting.SettingServer.entity.chat.ChatRoomMember;
import setting.SettingServer.entity.chat.ChatRoomMemberStatus;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.chat.ChatMessageRepository;
import setting.SettingServer.repository.chat.ChatRoomMemberRepository;
import setting.SettingServer.repository.chat.ChatRoomRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public ChatRoomDto createDirectChatRoom(Long userId1, Long userId2) {
        log.info("1:1 채팅방 생성 요청: user1={}, user2={}", userId1, userId2);

        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            throw new UnauthorizedException("채팅방 생성 권한이 없습니다");
        }

        Optional<ChatRoom> existingRoom = chatRoomRepository.findDirectChatRoom(userId1, userId2);
        if (existingRoom.isPresent()) {
            log.info("이미 존재하는 채팅방 반환: {}", existingRoom.get().getId());
            return mapToChatRoomDto(existingRoom.get(), currentUserId);
        }

        // 사용자 조회
        Member user1 = memberRepository.findById(userId1)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId1));

        Member user2 = memberRepository.findById(userId2)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId2));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.createDirectChat(user1, user2);
        chatRoomRepository.save(chatRoom);

        log.info("1:1 채팅방 생성 완료: {}", chatRoom.getRoomCode());
        return mapToChatRoomDto(chatRoom, currentUserId);
    }

    private ChatRoomDto mapToChatRoomDto(ChatRoom chatRoom, String currentUserId) {
        Member currentMember = memberRepository.findByName(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + currentUserId));

        String displayName = chatRoom.getDisplayNameForMember(currentMember);

        ChatMessage latestMessage = chatMessageRepository.findTopByChatRoomOrderByCreatedAtDesc(chatRoom).orElse(null);
        String latestMessageContent = null;
        LocalDateTime latestMessageTime = null;
        String latestMessageSenderId = null;
        String latestMessageSenderName = null;

        if (latestMessage != null) {
            latestMessageContent = latestMessage.getContent();
            latestMessageSenderName = latestMessage.getSender().getName();
        }

        long memberCount = chatRoomMemberRepository.countByChatRoomAndStatus(chatRoom, ChatRoomMemberStatus.ACTIVE);

        return new ChatRoomDto(chatRoom.getRoomCode(),
                displayName,
                chatRoom.getRoomType().name(),
                (int) memberCount,
                latestMessageContent,
                latestMessageTime,
                latestMessageSenderId,
                latestMessageSenderName
        );
    }
}
