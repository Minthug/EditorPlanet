package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.Reference;

public record ReferenceResponse(Long id, String title, String thumbnail, String videoUrl,
                                Double averageRating, Integer ratingCount, MemberResponse member) {

    public static ReferenceResponse from(Reference reference) {
        return new ReferenceResponse(
                reference.getId(),
                reference.getTitle(),
                reference.getThumbnail(),
                reference.getVideoUrl(),
                reference.getAverageRating(),
                reference.getRatingCount(),
                MemberResponse.of(reference.getAuthor())
        );
    }
}
