package setting.SettingServer.dto.chat;


import java.time.LocalDateTime;

/**
 * 채팅방 멤버 정보 DTO
 * @param id
 * @param userId
 * @param username
 * @param role OWNER, ADMIN, MEMBER
 * @param joinedAt
 * @param profileImageUrl
 */
public record ChatRoomMemberDto(Long id, String userId, String username, String role,
                                LocalDateTime joinedAt, String profileImageUrl) {
}
