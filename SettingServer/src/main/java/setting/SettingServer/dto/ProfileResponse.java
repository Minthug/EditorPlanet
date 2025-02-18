package setting.SettingServer.dto;

import setting.SettingServer.entity.Member;

public record ProfileResponse(Long id, String email, String name, String profileImage) {

    public static ProfileResponse fromMember(Member member) {
        return new ProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getImageUrl()
        );
    }
}
