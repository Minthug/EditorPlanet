package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.dto.chat.ChatRoomDto;
import setting.SettingServer.dto.chat.CreateDirectChatRoomRequest;
import setting.SettingServer.dto.chat.CreateGroupChatRoomRequest;
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


}

