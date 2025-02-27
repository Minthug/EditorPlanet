package setting.SettingServer.dto;

import setting.SettingServer.entity.Member;

public record MemberResponse(Long id, String email, String name, String imageUrl) {

    public static MemberResponse of(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getImageUrl()
                );
    }
}
