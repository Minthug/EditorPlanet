package setting.SettingServer.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.UnauthorizedException;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.dto.chat.*;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.*;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.chat.ChatMessageRepository;
import setting.SettingServer.repository.chat.ChatRoomMemberRepository;
import setting.SettingServer.repository.chat.ChatRoomRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public ChatRoomDto createGroupChatRoom(String name, String creatorId, List<Long> memberIds) {
        log.info("그룹 채팅방 생성 요청: name={}, creator={}, members={}", name, creatorId, memberIds);

        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(creatorId)) {
            throw new UnauthorizedException("채팅방 생성 권한이 없습니다");
        }

        if (memberIds.isEmpty()) {
            throw new IllegalArgumentException("그룹 채팅방은 최소 1명 이상의 멤버가 필요합니다");
        }

        Long creatorIdLong = Long.parseLong(creatorId);
        Member creator = memberRepository.findById(creatorIdLong)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다 " + creatorId));


        List<Member> members = new ArrayList<>();
        for (Long memberId : memberIds) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다" + memberId));
            members.add(member);
        }

        ChatRoom chatRoom = ChatRoom.createGroupChat(members, creator, name);
        chatRoomRepository.save(chatRoom);

        log.info("그룹 채팅방 생성 완료: {}", chatRoom.getRoomCode());
        return mapToChatRoomDto(chatRoom, currentUserId);
    }

    @Transactional
    public ChatRoomDetailDto getChatRoom(String roomCode, Long userId) {
        log.debug("채팅방 정보 조회: roomCode={}, userId={}", roomCode, userId);

        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + roomCode));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new UnauthorizedException("채팅방에 접근 권한이 없습니다"));

        if (!chatRoomMember.isActive()) {
            throw new UnauthorizedException("채팅방을 나갔거나 강퇴되었습니다");
        }

        ChatRoomDetailDto detailDto = mapToChatRoomDetailDto(chatRoom, userId);

        Page<ChatMessage> recentMessagePage = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, PageRequest.of(0, 20));

        List<ChatMessage> recentMessages = recentMessagePage.getContent();

        List<ChatMessageDto> messageDtos = recentMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(this::mapToChatMessageDto)
                .collect(Collectors.toList());

        detailDto = detailDto.withMessages(messageDtos);

        Long latestMessageId = recentMessages.stream()
                .max(Comparator.comparing(ChatMessage::getId))
                .map(ChatMessage::getId)
                .orElse(null);

        if (latestMessageId != null) {
            Long currentLastReadId = chatRoomMember.getLastReadMessageId();

            if (currentLastReadId == null || currentLastReadId < latestMessageId) {
                chatRoomMember.updateLastReadMessageId(latestMessageId);
                chatRoomMemberRepository.save(chatRoomMember);

                log.debug("채팅방 접근 시 자동 읽음 처리: roomCode={}, userId={], latestMessageId={]", roomCode, userId, latestMessageId);
            }
        }

        return detailDto;
    }

    @Transactional(readOnly = true)
    public ChatRoomListDto getChatRoomList(Long userId, int page, int size) {
        log.debug("채팅방 목록 조회: userId={}, page={], size={]", userId, page, size);

        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        PageRequest pageRequest = PageRequest.of(page -1 , size, Sort.by("createdAt").descending());

        Page<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByMemberAndStatusOrderByLastMessageTimestampDesc(member, ChatRoomMemberStatus.ACTIVE, pageRequest);

        List<ChatRoomDto> chatRoomDtos = chatRoomMembers.stream()
                .map(crm -> mapToChatRoomDto(crm.getChatRoom(), currentUserId))
                .collect(Collectors.toList());

        Map<String, Long> unreadCounts = getUnreadMessageCounts(currentUserId);

        return new ChatRoomListDto(
                chatRoomDtos,
                page,
                chatRoomMembers.getTotalPages(),
                chatRoomMembers.getTotalElements(),
                unreadCounts
        );
    }

    @Transactional
    public ChatMessageListDto getChatMessages(String roomCode, Long userId, int page, int size) {
        log.debug("채팅 메시지 조회: roomCode={], userId={}, page={}, size={}", roomCode, userId, page, size);

        // 현재 로그인한 사용자 확인
        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + roomCode));

        Long userIdLong = Long.parseLong(currentUserId);

        // 요청자가 채팅방 멤버인지 확인
        Member member = memberRepository.findById(userIdLong)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new UnauthorizedException("접근 권한이 없습니다"));

        if (!chatRoomMember.isActive()) {
            throw new UnauthorizedException("채팅방을 나갔거나 강퇴 되었습니다");
        }

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<ChatMessage> messagePage = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageRequest);

        List<ChatMessageDto> messageDtos = messagePage.getContent().stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(this::mapToChatMessageDto)
                .collect(Collectors.toList());

        return new ChatMessageListDto(
                messageDtos,
                page,
                messagePage.getTotalPages(),
                messagePage.getTotalElements()
        );
    }


    @Transactional
    public boolean inviteMember(String roomCode, Long inviterId, Long inviteeId) {
        log.info("채팅방 멤버 초대: roomCode={}, inviterId={}, inviteeId={}", roomCode, inviterId, inviteeId);

        // 현재 로그인한 사용자 확인
        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(inviterId)) {
            throw new UnauthorizedException("초대 권한이 없습니다");
        }

        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + roomCode));

        Member inviter = memberRepository.findById(inviterId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + inviterId));

        ChatRoomMember inviterMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, inviter)
                .orElseThrow(() -> new UnauthorizedException("채팅방에 접근 권한이 없습니다"));

        if (chatRoom.getRoomType() == ChatRoomType.DIRECT) {
            throw new IllegalStateException("1:1 채팅방에는 추가 멤버를 초대할 수 없습니다");
        }

        if (inviterMember.getRole() != ChatRoomMemberRole.OWNER && inviterMember.getRole() != ChatRoomMemberRole.ADMIN) {
            throw new UnauthorizedException("초대 권한이 없습니다");
        }

        Member invitee = memberRepository.findById(inviteeId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + inviteeId));

        // 이미 채팅방에 있는 멤버인지 확인
        Optional<ChatRoomMember> existingMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, invitee);
        if (existingMember.isPresent()) {
            if (existingMember.get().isActive()) {
                throw new IllegalStateException("이미 채팅방에 참여 중인 멤버입니다");
            } else {
                // 이전에 나갔던 멤버라면 재활성화
                ChatRoomMember member = existingMember.get();
                member.rejoin();
                chatRoomMemberRepository.save(member);

                // 시스템 메시지 추가
                createSystemMessage(chatRoom, invitee.getName() + "님이 채팅방에 참여했습니다.");

                return true;
            }
        }

        chatRoom.addMember(invitee, ChatRoomMemberRole.MEMBER);
        chatRoomRepository.save(chatRoom);

        createSystemMessage(chatRoom, invitee.getName() + "님이 채팅방에 참여했습니다");
        return true;
    }

    private void createSystemMessage(ChatRoom chatRoom, String content) {
        ChatMessage systemMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(null)
                .content(content)
                .messageType(MessageType.SERVER)
                .build();

        chatMessageRepository.save(systemMessage);
    }

    @Transactional
    public boolean leaveChatRoom(String roomCode, Long userId) {
        log.info("채팅방 나가기: roomCode={}, userId={}", roomCode, userId);


        // 현재 로그인한 사용자 확인
        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + roomCode));

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 멤버를 찾을 수 없습니다:"));

        if (!chatRoomMember.isActive()) {
            throw new IllegalStateException("이미 채팅방을 나갔습니다");
        }

        if (chatRoomMember.getRole() == ChatRoomMemberRole.OWNER && chatRoom.getRoomType() == ChatRoomType.GROUP) {
            List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findByChatRoomAndStatusOrderByCreatedAtAsc(chatRoom, ChatRoomMemberStatus.ACTIVE);

            ChatRoomMember newOwner = activeMembers.stream()
                    .filter(m -> !m.getMember().getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (newOwner != null) {
                newOwner.updateRole(ChatRoomMemberRole.OWNER);
                chatRoomMember.updateRole(ChatRoomMemberRole.MEMBER);
                chatRoomMemberRepository.save(newOwner);

                createSystemMessage(chatRoom, "방장 권한이 " + newOwner.getMember().getName() + "님에게 이전 되었습니다");
            }
        }

        chatRoomMember.leave();
        chatRoomMemberRepository.save(chatRoomMember);

        createSystemMessage(chatRoom, member.getName() + "님이 채팅방을 나갔습니다");

        long activeMembers = chatRoomMemberRepository.countByChatRoomAndStatus(chatRoom, ChatRoomMemberStatus.ACTIVE);
        if (activeMembers == 0) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
        }

        return true;
    }

    @Transactional
    public boolean updateChatRoomName(String roomCode, Long userId, String name) {
        log.info("채팅방 이름 변경: roomCode={}, userId={}, name={}", roomCode, userId, name);


        // 현재 로그인한 사용자 확인
        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다: " + roomCode));

        // 사용자 조회
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + userId));

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 멤버를 찾을 수 없습니다"));

        if (!chatRoomMember.isActive()) {
            throw new UnauthorizedException("채팅방을 나갔거나 강퇴되었습니다");
        }

        if (chatRoom.getRoomType() == ChatRoomType.DIRECT) {
            chatRoomMember.updateNickname(name);
            chatRoomMemberRepository.save(chatRoomMember);
        } else {
            if (chatRoomMember.getRole() != ChatRoomMemberRole.OWNER && chatRoomMember.getRole() != ChatRoomMemberRole.ADMIN) {
                throw new UnauthorizedException("권한이 없습니다");
            }

            chatRoom.updateCustomName(name);
            chatRoomRepository.save(chatRoom);

            createSystemMessage(chatRoom, member.getName() + "님이 채팅방 이름을 '" + name + "'(으)로 변경 했습니다");
        }

        return true;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadMessageCounts(String userId) {
        log.debug("안 읽은 메시지 수 조화: userId={]", userId);

        String currentUserId = securityUtil.getCurrentMemberUsername();
        if (userId != null && !currentUserId.equals(userId)) {
            throw new UnauthorizedException("접근 권한이 없습니다");
        }

        Long userIdLong = Long.parseLong(currentUserId);

        Member member = memberRepository.findById(userIdLong)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + currentUserId));

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByMemberAndStatus(member, ChatRoomMemberStatus.ACTIVE);

        Map<String, Long> unreadCounts = new HashMap<>();

        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            String roomCode = chatRoomMember.getChatRoom().getRoomCode();
            Long lastReadMessageId = chatRoomMember.getLastReadMessageId();

            long unreadCount;
            if (lastReadMessageId == null) {
                unreadCount = chatMessageRepository.countByChatRoomAndMessageTypeNot(chatRoomMember.getChatRoom(), MessageType.SERVER);
            } else {
                unreadCount = chatMessageRepository.countByChatRoomAndIdGreaterThanAndMessageTypeNot(chatRoomMember.getChatRoom(), lastReadMessageId, MessageType.SERVER);
            }

            unreadCounts.put(roomCode, unreadCount);
        }
        return unreadCounts;
    }

    private ChatMessageDto mapToChatMessageDto(ChatMessage message) {

        String senderId = null;
        String senderName = null;

        if (message.getSender() != null) {
            senderId = message.getSender().getUserId().toString();
            senderName = message.getSender().getName();
        }

        return new ChatMessageDto(
                message.getId(),
                message.getContent(),
                message.getMessageType().name(),
                senderId,
                senderName,
                message.getCreatedAt());
    }

    private ChatRoomDetailDto mapToChatRoomDetailDto(ChatRoom chatRoom, Long currentUserId) {

        Member currentMember = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + currentUserId));

        String displayName = chatRoom.getDisplayNameForMember(currentMember);

        List<ChatRoomMemberDto> memberDtos = chatRoomMemberRepository.findByChatRoomAndStatusOrderByCreatedAtAsc(chatRoom, ChatRoomMemberStatus.ACTIVE).stream()
                .map(member -> new ChatRoomMemberDto(
                        member.getId(),
                        member.getMember().getUserId().toString(),
                        member.getMember().getName(),
                        member.getRole().name(),
                        member.getJoinedAt(),
                        member.getMember().getImageUrl()))
                .collect(Collectors.toList());

        return new ChatRoomDetailDto(
                chatRoom.getRoomCode(),
                displayName,
                chatRoom.getRoomType().name(),
                chatRoom.getCreatedAt(),
                memberDtos,
                Collections.emptyList());
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

            if (latestMessage.getSender() != null) {
                latestMessageSenderId = latestMessage.getSender().getUserId().toString();
                latestMessageSenderName = latestMessage.getSender().getName();
            }
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
