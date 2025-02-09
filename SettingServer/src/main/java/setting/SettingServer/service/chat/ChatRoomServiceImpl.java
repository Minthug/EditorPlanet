package setting.SettingServer.service.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import setting.SettingServer.common.chat.ChatRoomAccessDeniedException;
import setting.SettingServer.common.chat.ChatRoomNotFoundException;
import setting.SettingServer.common.exception.UserNotFoundException;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.dto.chat.*;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.chat.ChatMessage;
import setting.SettingServer.entity.chat.ChatRoom;
import setting.SettingServer.repository.chat.ChatMessageRepository;
import setting.SettingServer.repository.chat.ChatRoomRepository;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.chat.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SecurityUtil securityUtil;

    @Override
    public CreatedChatRoomResponse createChatRoomForPersonal(CreatedChatRoomRequest chatRoomRequest) {
        String id = securityUtil.getCurrentMemberUsername();
        if (!id.equals(chatRoomRequest.getRoomMakerId())) {
            throw new UserNotFoundException("");
        }

        String sharedChatRoomId = chatRoomRepository.findSharedChatRoom(chatRoomRequest.getGuestId(), chatRoomRequest.getRoomMakerId());
        if (sharedChatRoomId != null) {
            return new CreatedChatRoomResponse(chatRoomRequest.getRoomMakerId(), chatRoomRequest.getGuestId(), sharedChatRoomId);
        }
        Member roomMaker = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));
        Member guest = userRepository.findById(chatRoomRequest.getGuestId()).orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다"));

        ChatRoom newRoom = new ChatRoom();
        newRoom.addMembers(roomMaker, guest);

        chatRoomRepository.save(newRoom);

        return new CreatedChatRoomResponse(roomMaker.getUserId(), guest.getUserId(), newRoom.getId());
    }

    @Override
    public ChatRoomInfoResponse chatRoomInfo(String roomId, int page, int size) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("채팅방을 찾을 수 없습니다"));
        ChatRoomInfoResponse chatRoomInfoResponse = new ChatRoomInfoResponse(chatRoom);

        Set<ChatUserInfoDto> chatRoomMembers = chatRoomInfoResponse.getChatRoomMembers();
        if (!chatRoomMembers.contains(new ChatUserInfoDto(userRepository.findById(securityUtil.getCurrentMemberUsername()).get()))) {
            throw new ChatRoomAccessDeniedException("권한이 없습니다.");
        }

        List<ChatMessage> latestChatMessages = findChatMessagesWithPaging(page, size, roomId);
        List<ChatMessageInfo> chatMessageInfos = new ArrayList<>();

        for (ChatMessage chatMessage : latestChatMessages) {
            chatMessageInfos.add(new ChatMessageInfo(chatMessage));
        }

        chatRoomInfoResponse.setLatestChatMessages(chatMessageInfos);
        return chatRoomInfoResponse;
    }

    private List<ChatRoom> findChatRoomsWithPaging(int page, int size, String userId) {
        Sort sort = Sort.by("lastChatMesg.createdAt").descending();
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        List<ChatRoom> chatRooms = chatRoomRepository.findByChatRoomMembersId(userId, pageRequest).getContent();

        return chatRooms;
    }

    public List<ChatMessage> findChatMessagesWithPaging(int page, int size, String roomId) {
        Sort sort = Sort.by("createdAt").descending();
        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        List<ChatMessage> result = chatMessageRepository.findListsByRoomId(roomId, pageRequest).getContent();

        return result;
    }

    @Override
    public ChatRoom findById(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow();
    }

    @Override
    public ChatRoomListResponse getChatRoomList(int page, int size) {
        String userId = securityUtil.getCurrentMemberUsername();

        List<ChatRoom> chatRooms = findChatRoomsWithPaging(page, size, userId);
        List<ChatRoomList> chatRoomInfo = new ArrayList<>();
        for (ChatRoom chatRoom : chatRooms) {
            chatRoomInfo.add(new ChatRoomList(chatRoom, userId));
        }

        ChatRoomListResponse info = ChatRoomListResponse.builder()
                .page(page)
                .count(chatRooms.size())
                .reqUserId(userId)
                .chatRooms(chatRoomInfo)
                .build();
        return info;
    }
}
