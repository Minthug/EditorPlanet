package setting.SettingServer.service.request;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.Reference;

public record ReferenceCreateRequest(Long memberId, String title, String thumbnail, String videoUrl,
                                      Double averageRating, Integer ratingCount, MemberResponse member) {

    public static ReferenceCreateRequest from(Reference reference) {
        return new ReferenceCreateRequest(
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
