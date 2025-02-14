package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponseDto;
import setting.SettingServer.entity.Reference;

public record ReferenceResponse(Long id, String title, String thumbnail, String videoUrl,
                                Double averageRating, Integer ratingCount, MemberResponseDto member) {

    public static ReferenceResponse from(Reference reference) {
        return new ReferenceResponse(
                reference.getId(),
                reference.getTitle(),
                reference.getThumbnail(),
                reference.getVideoUrl(),
                reference.getAverageRating(),
                reference.getRatingCount(),
                MemberResponseDto.of(reference.getAuthor())
        );
    }
}
