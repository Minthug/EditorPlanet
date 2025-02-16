package setting.SettingServer.service.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RatingCreateRequest(Long referenceId, Long memberId, @Min(0) @Max(5) Integer score) {
}
