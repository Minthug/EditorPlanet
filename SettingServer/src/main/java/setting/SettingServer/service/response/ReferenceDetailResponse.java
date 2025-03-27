package setting.SettingServer.service.response;

import lombok.Builder;
import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.entity.Reference;
import setting.SettingServer.repository.ScoreCount;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReferenceDetailResponse(Long id, String title, String thumbnail,
                                      String videoUrl, MemberResponse author,
                                      int viewCount, Double averageRating, Integer ratingCount,
                                      Integer userRating, List<ScoreCount> ratingDistribution,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {

    public static ReferenceDetailResponse fromWithRating(Reference reference, Integer userRating, List<ScoreCount> ratingDistribution) {
        return ReferenceDetailResponse.builder()
                .id(reference.getId())
                .title(reference.getTitle())
                .thumbnail(reference.getThumbnail())
                .videoUrl(reference.getVideoUrl())
                .author(MemberResponse.of(reference.getAuthor()))
                .viewCount(reference.getViewCount())
                .averageRating(reference.getAverageRating())
                .ratingCount(reference.getRatingCount())
                .createdAt(reference.getCreatedAt())
                .updatedAt(reference.getUpdatedAt())
                .build();
    }
}


