package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.dto.chat.*;
import setting.SettingServer.service.chat.ChatRoomService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/chat/rooms")
@Slf4j
public class ChatRoomController {


    private final ChatRoomService chatRoomService;

    /**
     * 1:1 채팅방 생성
     * @param request
     * @return
     */
    @PostMapping("/direct")
    public ResponseEntity<ChatRoomDto> createDirectChatRoom(@RequestBody @Valid CreateDirectChatRoomRequest request) {
        log.info("1:1 채팅방 생성 요청: {}", request);
        ChatRoomDto chatRoom = chatRoomService.createDirectChatRoom(request.userId(), request.targetUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoom);
    }

    /**
     * 그룹 채팅방 생성
     * @param request
     * @return
     */
    @PostMapping("/group")
    public ResponseEntity<ChatRoomDto> createGroupChatRoom(@RequestBody @Valid CreateGroupChatRoomRequest request) {
        log.info("그룹 채팅방 생성 요청: {}", request);

        ChatRoomDto chatRoomDto = chatRoomService.createGroupChatRoom(request.name(), request.creatorId(), request.memberIds());

        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomDto);
    }

    /**
     * 채팅방 상세 조회
     * @param roomCode
     * @param userId
     * @return
     */
    @GetMapping("/{roomCode}")
    public ResponseEntity<ChatRoomDetailDto> getChatRoom(@PathVariable String roomCode,
                                                         @RequestParam Long userId) {
        log.info("채팅방 정보 조회: roomCode={}, userId={}", roomCode, userId);

        ChatRoomDetailDto chatRoom = chatRoomService.getChatRoom(roomCode, userId);
        return ResponseEntity.ok(chatRoom);
    }

    /**
     * 사용자의 채팅방 목록 조회
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<ChatRoomListDto> getChatRooms(@RequestParam Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        log.info("채팅방 목록 조회: userId={}, page={}, size={}", userId, page, size);

        ChatRoomListDto chatRooms = chatRoomService.getChatRoomList(userId, page, size);

        return ResponseEntity.ok(chatRooms);
    }

    /**
     * 채팅방 메시지 조회
     * @param roomCode
     * @param userId
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/{roomCode}/messages")
    public ResponseEntity<ChatMessageListDto> getChatMessages(@PathVariable String roomCode,
                                                              @RequestParam Long userId,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        log.info("채팅 메시지 조회: roomCode={}, userId={}, page={}, size={}", roomCode, userId, page, size);

        ChatMessageListDto messages = chatRoomService.getChatMessages(roomCode, userId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅방 멤버 초대
     * @param roomCode
     * @param request
     * @return
     */
    @PostMapping("/{roomCode}/invite")
    public ResponseEntity<Boolean> inviteMember(@PathVariable String roomCode,
                                                @RequestBody @Valid InviteMemberRequest request) {
        log.info("채팅방 멤버 초대: roomCode={}, request={}", roomCode, request);

        boolean result = chatRoomService.inviteMember(roomCode,
                request.inviterId(),
                request.inviteeId());

        return ResponseEntity.ok(result);
    }

    /**
     * 채팅방 나가기
     * @return
     */
    @PostMapping("/{roomCode}/leave")
    public ResponseEntity<Boolean> leaveChatRoom(@PathVariable String roomCode,
                                                 @RequestParam Long userId) {
        log.info("채팅방 나가기: roomCode={}, userId={}", roomCode, userId);

        boolean result = chatRoomService.leaveChatRoom(roomCode, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * 채팅방 이름 변경
     * @return
     */
    @PatchMapping("/{roomCode}/name")
    public ResponseEntity<Boolean> updateChatRoomName(@PathVariable String roomCode,
                                                      @RequestBody @Valid UpdateChatRoomNameRequest request) {
        log.info("채팅방 이름 변경: roomCode={}, request={}", roomCode, request);

        boolean result = chatRoomService.updateChatRoomName(roomCode,
                request.userId(),
                request.name());

        return ResponseEntity.ok(result);
    }

    /**
     * 채팅방 참여자 목록 조회
     * @return
     */
    @GetMapping("/{roomCode}/members")
    public ResponseEntity<List<ChatRoomMemberDto>> getChatRoomMembers(@PathVariable String roomCode,
                                                                      @RequestParam Long userId) {
        log.info("채팅방 멤버 목록 조회: roomCode={}, userId={}", roomCode, userId);
        List<ChatRoomMemberDto> memberDtos = chatRoomService.getChatRoomMember(roomCode, userId);

        return ResponseEntity.ok(memberDtos);
    }



    public ResponseEntity<Boolean> markMessageAsRead() {

    }

    public ResponseEntity<Map<String, Long>> getUnreadMessageCounts() {

    }
}

