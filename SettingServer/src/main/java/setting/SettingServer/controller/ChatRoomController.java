package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.dto.chat.*;
import setting.SettingServer.service.chat.ChatRoomService;

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

    @GetMapping("/list")
    public ResponseEntity<ChatRoomListDto> getChatRooms(@RequestParam Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        log.info("채팅방 목록 조회: userId={}, page={}, size={}", userId, page, size);

        ChatRoomListDto chatRooms = chatRoomService.getChatRoomList(userId, page, size);

        return ResponseEntity.ok(chatRooms);
    }


}

