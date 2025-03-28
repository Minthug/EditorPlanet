package setting.SettingServer.service.response;

import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.Reference;
import setting.SettingServer.repository.ScoreCount;

import java.time.LocalDateTime;
import java.util.List;

public record ReferenceDetailResponse(Long id, String title, String thumbnail,
                                      String videoUrl, MemberResponse author,
                                      int viewCount, Double averageRating, Integer ratingCount,
                                      Integer userRating, List<ScoreCount> ratingDistribution,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static ReferenceDetailResponse fromWithRating(Reference reference, Integer userRating, List<ScoreCount> ratingDistribution) {
        return new ReferenceDetailResponse(
                reference.getId(),
                reference.getTitle(),
                reference.getThumbnail(),
                reference.getVideoUrl(),
                MemberResponse.of(reference.getAuthor()),
                reference.getViewCount(),
                reference.getAverageRating(),
                reference.getRatingCount(),
                userRating,
                ratingDistribution,
                reference.getCreatedAt(),
                reference.getUpdatedAt());
    }
}


