package setting.SettingServer.service.request;

import setting.SettingServer.dto.MemberResponseDto;
import setting.SettingServer.entity.Reference;

public record ReferenceCreateRequest(Long memberId, String title, String thumbnail, String videoUrl,
                                      Double averageRating, Integer ratingCount, MemberResponseDto member) {

    public static ReferenceCreateRequest from(Reference reference) {
        return new ReferenceCreateRequest(
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
