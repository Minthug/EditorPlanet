package setting.SettingServer.service.chat;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
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


    // ================= 비즈니스 메서드 (개선된 버전) =================

    /**
     * 1:1 채팅방 생성
     */
    @Transactional
    public ChatRoomDto createDirectChatRoom(Long userId1, Long userId2) {
        log.info("1:1 채팅방 생성 요청: user1={}, user2={}", userId1, userId2);

        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            throw new UnauthorizedException("채팅방 생성 권한이 없습니다");
        }

        Optional<ChatRoom> existingRoom = chatRoomRepository.findDirectChatRoom(userId1, userId2);
        if (existingRoom.isPresent()) {
            log.info("이미 존재하는 채팅방 반환: {}", existingRoom.get().getId());
            return mapToChatRoomDto(existingRoom.get(), currentUserId);
        }

        // 사용자 조회
        Member user1 = getMemberById(userId1);
        Member user2 = getMemberById(userId2);

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.createDirectChat(user1, user2);
        chatRoomRepository.save(chatRoom);

        log.info("1:1 채팅방 생성 완료: {}", chatRoom.getRoomCode());
        return mapToChatRoomDto(chatRoom, currentUserId);
    }

    /**
     * 그룹 채팅방 생성
     */
    @Transactional
    public ChatRoomDto createGroupChatRoom(String name, Long creatorId, List<Long> memberIds) {
        log.info("그룹 채팅방 생성 요청: name={}, creator={}, members={}", name, creatorId, memberIds);

        validateCurrentUser(creatorId);

        if (memberIds.isEmpty()) {
            throw new IllegalArgumentException("그룹 채팅방은 최소 1명 이상의 멤버가 필요합니다");
        }

        Member creator = getMemberById(creatorId);

        List<Member> members = memberIds.stream()
                .map(this::getMemberById)
                .collect(Collectors.toList());

        ChatRoom chatRoom = ChatRoom.createGroupChat(members, creator, name);
        chatRoomRepository.save(chatRoom);

        log.info("그룹 채팅방 생성 완료: {}", chatRoom.getRoomCode());
        return mapToChatRoomDto(chatRoom, creatorId);
    }

    /**
     * 채팅방 정보 조회
     */
    @Transactional
    public ChatRoomDetailDto getChatRoom(String roomCode, Long userId) {
        log.debug("채팅방 정보 조회: roomCode={}, userId={}", roomCode, userId);

        validateCurrentUser(userId);

        Pair<ChatRoom, ChatRoomMember> result = getChatRoomAndValidateMember(roomCode, userId);
        ChatRoom chatRoom = result.getFirst();
        ChatRoomMember chatRoomMember = result.getSecond();

        ChatRoomDetailDto detailDto = mapToChatRoomDetailDto(chatRoom, userId);

        Page<ChatMessage> recentMessagePage = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, PageRequest.of(0, 20));

        List<ChatMessage> recentMessages = recentMessagePage.getContent();

        List<ChatMessageDto> messageDtos = recentMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(this::mapToChatMessageDto)
                .collect(Collectors.toList());

        detailDto = detailDto.withMessages(messageDtos);

        updateLastReadMessageIfNewer(chatRoomMember, recentMessages, roomCode, userId);

        return detailDto;
    }

    /**
     * 최신 메시지 읽음 처리(자동)
     */
    private void updateLastReadMessageIfNewer(ChatRoomMember chatRoomMember, List<ChatMessage> messages, String roomCode, Long userId) {
        Long latestMessageId = messages.stream()
                .max(Comparator.comparing(ChatMessage::getId))
                .map(ChatMessage::getId)
                .orElse(null);
    }

    /**
     * 사용자의 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomListDto getChatRoomList(Long userId, int page, int size) {
        log.debug("채팅방 목록 조회: userId={}, page={], size={]", userId, page, size);

        validateCurrentUser(userId);
        Member member = getMemberById(userId);

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByMemberAndStatusOrderByLastMessageTimestampDesc(
                member, ChatRoomMemberStatus.ACTIVE, pageRequest);

        List<ChatRoomDto> chatRoomDtos = chatRoomMembers.stream()
                .map(crm -> mapToChatRoomDto(crm.getChatRoom(), userId))
                .collect(Collectors.toList());

        Map<String, Long> unreadCounts = getUnreadMessageCounts(userId);

        return new ChatRoomListDto(
                chatRoomDtos,
                page,
                chatRoomMembers.getTotalPages(),
                chatRoomMembers.getTotalElements(),
                unreadCounts
        );
    }


    /**
     * 채팅방 메시지 조회
     */
    @Transactional
    public ChatMessageListDto getChatMessages(String roomCode, Long userId, int page, int size) {
        log.debug("채팅 메시지 조회: roomCode={], userId={}, page={}, size={}", roomCode, userId, page, size);

        validateCurrentUser(userId);

        // 채팅방 및 멤버 조회 및 검증
        Pair<ChatRoom, ChatRoomMember> result = getChatRoomAndValidateMember(roomCode, userId);
        ChatRoom chatRoom = result.getFirst();

        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());

        Page<ChatMessage> messagePage = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, pageRequest);

        // 메시지 매핑
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

    /**
     * 채팅방에 멤버 초대
     */
    @Transactional
    public boolean inviteMember(String roomCode, Long inviterId, Long inviteeId) {
        log.info("채팅방 멤버 초대: roomCode={}, inviterId={}, inviteeId={}", roomCode, inviterId, inviteeId);

        validateCurrentUser(inviterId);

        ChatRoom chatRoom = getChatRoomByCode(roomCode);

        Member inviter = getMemberById(inviterId);
        ChatRoomMember inviterMember = validateActiveMember(chatRoom, inviter);


        if (chatRoom.getRoomType() == ChatRoomType.DIRECT) {
            throw new IllegalStateException("1:1 채팅방에는 추가 멤버를 초대할 수 없습니다");
        }

        if (inviterMember.getRole() != ChatRoomMemberRole.OWNER && inviterMember.getRole() != ChatRoomMemberRole.ADMIN) {
            throw new UnauthorizedException("초대 권한이 없습니다");
        }

        Member invitee = getMemberById(inviteeId);

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

    /**
     * 채팅방 나가기
     */
    @Transactional
    public boolean leaveChatRoom(String roomCode, Long userId) {
        log.info("채팅방 나가기: roomCode={}, userId={}", roomCode, userId);

        validateCurrentUser(userId);

        ChatRoom chatRoom = getChatRoomByCode(roomCode);
        Member member = getMemberById(userId);

        ChatRoomMember chatRoomMember = chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new EntityNotFoundException("채팅방 멤버를 찾을 수 없습니다:"));

        if (!chatRoomMember.isActive()) {
            throw new IllegalStateException("이미 채팅방을 나갔습니다");
        }

        transferOwnershipIfNeeded(chatRoom, chatRoomMember, userId);

        chatRoomMember.leave();
        chatRoomMemberRepository.save(chatRoomMember);

        createSystemMessage(chatRoom, member.getName() + "님이 채팅방을 나갔습니다");

        deactivateChatRoomIfEmpty(chatRoom);

        return true;
    }


    /**
     * 방장이 나갈 때 권한 이전 처리
     */
    private void transferOwnershipIfNeeded(ChatRoom chatRoom, ChatRoomMember chatRoomMember, Long userId) {
        if (chatRoomMember.getRole() == ChatRoomMemberRole.OWNER && chatRoom.getRoomType() == ChatRoomType.GROUP) {
            // 다른 활성 멤버 찾기
            List<ChatRoomMember> activeMembers = chatRoomMemberRepository.findByChatRoomAndStatusOrderByCreatedAtAsc(
                    chatRoom, ChatRoomMemberStatus.ACTIVE);

            ChatRoomMember newOwner = activeMembers.stream()
                    .filter(m -> !m.getMember().getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (newOwner != null) {
                // 방장 권한 이전
                newOwner.updateRole(ChatRoomMemberRole.OWNER);
                chatRoomMember.updateRole(ChatRoomMemberRole.MEMBER);
                chatRoomMemberRepository.save(newOwner);

                // 시스템 메시지 추가
                createSystemMessage(chatRoom, "방장 권한이 " + newOwner.getMember().getName() + "님에게 이전되었습니다.");
            }
        }
    }

    /**
     * 채팅방 비활성화
     */
    private void deactivateChatRoomIfEmpty(ChatRoom chatRoom) {
        long activeMembers = chatRoomMemberRepository.countByChatRoomAndStatus(chatRoom, ChatRoomMemberStatus.ACTIVE);
        if (activeMembers == 0) {
            chatRoom.deactivate();
            chatRoomRepository.save(chatRoom);
        }
    }


    /**
     * 채팅방 이름 변경
     */
    @Transactional
    public boolean updateChatRoomName(String roomCode, Long userId, String name) {
        log.info("채팅방 이름 변경: roomCode={}, userId={}, name={}", roomCode, userId, name);

        validateCurrentUser(userId);

        Pair<ChatRoom, ChatRoomMember> result = getChatRoomAndValidateMember(roomCode, userId);
        ChatRoom chatRoom = result.getFirst();
        ChatRoomMember chatRoomMember = result.getSecond();

        Member member = getMemberById(userId);

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

    /**
     * 채팅방 참여자 목록 조회
     */
    @Transactional
    public List<ChatRoomMemberDto> getChatRoomMember(String roomCode, Long userId) {
        log.debug("채팅방 멤버 목록 조회: roomCode={}, userId={}", roomCode, userId);

        validateCurrentUser(userId);

        Pair<ChatRoom, ChatRoomMember> result = getChatRoomAndValidateMember(roomCode, userId);
        ChatRoom chatRoom = result.getFirst();

        // 활성 멤버 목록 조회
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomAndStatusOrderByCreatedAtAsc(
                chatRoom, ChatRoomMemberStatus.ACTIVE);

        // DTO로 변환
        return members.stream()
                .map(this::mapToChatRoomMemberDto)
                .collect(Collectors.toList());
    }

    /**
     * 메시지 읽음 처리
     */
    @Transactional
    public boolean markMessageAsRead(String roomCode, Long userId, Long messageId) {
        log.debug("메시지 읽음 처리: roomCode={}, userId={}, messageId={}", roomCode, userId, messageId);

        validateCurrentUser(userId);

        Pair<ChatRoom, ChatRoomMember> result = getChatRoomAndValidateMember(roomCode, userId);
        ChatRoom chatRoom = result.getFirst();
        ChatRoomMember chatRoomMember = result.getSecond();


        // 메시지 조회
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("메시지를 찾을 수 없습니다: " + messageId));

        // 메시지가 해당 채팅방의 것인지 확인
        if (!message.getChatRoom().equals(chatRoom)) {
            throw new IllegalArgumentException("메시지가 해당 채팅방의 것이 아닙니다");
        }

        // 메시지 읽음 처리
        chatRoomMember.updateLastReadMessageId(messageId);
        chatRoomMemberRepository.save(chatRoomMember);

        return true;
    }

    @Transactional
    public boolean markAllMessagesAsRead(String roomCode, Long userId) {
        log.debug("채팅방 전체 메시지 읽음 처리: roomCode={}, userId={}", roomCode, userId);

        validateCurrentUser(userId);

        Pair<ChatRoom, ChatRoomMember> result = getChatRoomAndValidateMember(roomCode, userId);
        ChatRoom chatRoom = result.getFirst();
        ChatRoomMember chatRoomMember = result.getSecond();

        List<ChatMessage> recentMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtDesc(chatRoom, PageRequest.of(0, 1)).getContent();

        if (recentMessages.isEmpty()) {
            log.debug("채팅방에 메시지가 없습니다: roomCode={]", roomCode);
            return true;
        }

        ChatMessage latestMessages = recentMessages.get(0);
        Long latestMessageId = latestMessages.getId();

        if (chatRoomMember.getLastReadMessageId() != null && chatRoomMember.getLastReadMessageId() >= latestMessageId) {
            log.debug("이미 최신 메시지까지 읽었습니다: roomCode={}, userId={}", roomCode, userId);

            return true;
        }

        chatRoomMember.updateLastReadMessageId(latestMessageId);
        chatRoomMemberRepository.save(chatRoomMember);

        log.info("채팅방 전체 메시지 읽음 처리 완료: roomCode={}, userId={}", roomCode, userId);
        return true;
    }

    /**
     * 안 읽은 메시지 수 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUnreadMessageCounts(Long userId) {
        log.debug("안 읽은 메시지 수 조화: userId={]", userId);

        validateCurrentUser(userId);
        Member member = getMemberById(userId);

        List<ChatRoomMember> chatRoomMembers = chatRoomMemberRepository.findByMemberAndStatus(
                member, ChatRoomMemberStatus.ACTIVE);

        Map<String, Long> unreadCounts = new HashMap<>();

        for (ChatRoomMember chatRoomMember : chatRoomMembers) {
            String roomCode = chatRoomMember.getChatRoom().getRoomCode();
            unreadCounts.put(roomCode, getUnreadCount(chatRoomMember));
        }
        return unreadCounts;
    }

    /**
     * 특정 채팅방의 안 읽은 메시지 수 계산
     * @return
     */
    private Long getUnreadCount(ChatRoomMember chatRoomMember) {
        Long lastReadMessageId = chatRoomMember.getLastReadMessageId();
        ChatRoom chatRoom = chatRoomMember.getChatRoom();

        if (lastReadMessageId == null) {
            return chatMessageRepository.countByChatRoomAndMessageTypeNot(chatRoom, MessageType.SERVER);
        } else {
            return chatMessageRepository.countByChatRoomAndIdGreaterThanAndMessageTypeNot(chatRoom, lastReadMessageId, MessageType.SERVER);
        }
    }

    // ================= 매핑 메서드 =================


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

    private ChatRoomDetailDto mapToChatRoomDetailDto(ChatRoom chatRoom, Long userId) {

        Member currentMember = getMemberById(userId);

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

    private ChatRoomDto mapToChatRoomDto(ChatRoom chatRoom, Long userId) {

        Member currentMember = getMemberById(userId);

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

    private ChatRoomMemberDto mapToChatRoomMemberDto(ChatRoomMember member) {

        String profileImageUrl = null;
        if (member.getMember().getImageUrl() != null) {
            profileImageUrl = member.getMember().getImageUrl();
        }

        return new ChatRoomMemberDto(
                member.getId(),
                member.getMember().getUserId().toString(),
                member.getMember().getName(),
                member.getRole().name(),
                member.getJoinedAt(),
                profileImageUrl
        );
    }

    // ================= 공통 헬퍼 메서드 =================

    /**
     * 현재 로그인한 사용자의 ID를 Long 타입으로 반환
     */
    private Long getCurrentUserId() {
        return Long.parseLong(securityUtil.getCurrentMemberUsername());
    }

    /**
     * 권한 확인: 현재 사용자와 요청 사용자가 일치하는지 검증
     */
    private void validateCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("권한이 없습니다");
        }
    }

    /**
     * RoomCode로 채팅방 조회
     */
    private ChatRoom getChatRoomByCode(String roomCode) {
        return chatRoomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다"));
    }

    /**
     * 사용자 ID로 Member 조회
     */
    private Member getMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));
    }

    private ChatRoomMember getChatRoomMember(ChatRoom chatRoom, Member member) {
        return chatRoomMemberRepository.findByChatRoomAndMember(chatRoom, member)
                .orElseThrow(() -> new UnauthorizedException("채팅방에 접근할 권한이 없습니다"));
    }


    /**
     * 활성 채팅방 멤버인지 확인
     */
    private ChatRoomMember validateActiveMember(ChatRoom chatRoom, Member member) {
        ChatRoomMember chatRoomMember = getChatRoomMember(chatRoom, member);

        if (!chatRoomMember.isActive()) {
            throw new UnauthorizedException("채팅방을 나갔거나 강퇴 되었습니다");
        }

        return chatRoomMember;
    }

    /**
     * 채팅방과 사용자 ID로 활성 멤버인지 확인
     */
    private ChatRoomMember validateActiveMember(ChatRoom chatRoom, Long userId) {
        Member member = getMemberById(userId);
        return validateActiveMember(chatRoom, member);
    }

    /**
     * RoomCode 와 사용자 ID로 채팅방과 활성 멤버 조회
     */
    private Pair<ChatRoom, ChatRoomMember> getChatRoomAndValidateMember(String roomCode, Long userId) {
        ChatRoom chatRoom = getChatRoomByCode(roomCode);
        ChatRoomMember chatRoomMember = validateActiveMember(chatRoom, userId);

        return Pair.of(chatRoom, chatRoomMember);
    }

}
