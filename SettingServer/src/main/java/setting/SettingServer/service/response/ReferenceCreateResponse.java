package setting.SettingServer.service.response;

import org.springframework.cglib.core.Local;
import setting.SettingServer.dto.MemberResponseDto;
import setting.SettingServer.entity.Reference;

import java.time.LocalDateTime;

public record ReferenceCreateResponse(Long memberId, String title, String thumbnail, String videoUrl,
                                      Double averageRating, Integer ratingCount, MemberResponseDto member) {

    public static ReferenceCreateResponse from(Reference reference) {
        return new ReferenceCreateResponse(
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
