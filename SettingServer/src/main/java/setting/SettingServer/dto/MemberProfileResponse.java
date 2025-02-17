package setting.SettingServer.dto;

import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.ProviderType;

import java.io.Serializable;

public record MemberProfileResponse(Long id, String email, String name, String imageUrl, ProviderType providerType) implements Serializable {

    public static MemberProfileResponse from(Member member) {
        String finalImageUrl = determineProfileImageUrl(member);

        return new MemberProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getName(),
                finalImageUrl,
                member.getType()
        );
    }

    private static String determineProfileImageUrl(Member member) {
        if (isOAuthProvider(member.getType())) {
            member.updateProfileImageUrl(member.getImageUrl());
            return member.getImageUrl();
        }

        if (member.getImageUrl() == null || member.getImageUrl().isEmpty()) {
            String defaultImageUrl = "";
            member.updateProfileImageUrl(defaultImageUrl);
            return defaultImageUrl;
        }

        return member.getImageUrl();
    }

    private static boolean isOAuthProvider(ProviderType type) {
        return type == ProviderType.GOOGLE ||
                type == ProviderType.KAKAO ||
                type == ProviderType.NAVER;
    }
}
