package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.Reference;

public record ReferenceListResponse(Long id, String title, String thumbnail, Double averageRating, MemberResponse response) {

    public static ReferenceListResponse from(Reference reference) {
        return new ReferenceListResponse(
                reference.getId(),
                reference.getTitle(),
                reference.getThumbnail(),
                reference.getAverageRating(),
                MemberResponse.of(reference.getAuthor())
        );
    }
}
