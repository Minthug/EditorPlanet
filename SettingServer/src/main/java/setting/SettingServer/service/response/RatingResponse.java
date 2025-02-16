package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.Rating;

public record RatingResponse(Long id, Integer score, MemberResponse response) {

    public static RatingResponse from(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getScore(),
                MemberResponse.of(rating.getMember())
        );
    }
}
