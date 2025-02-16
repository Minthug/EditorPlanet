package setting.SettingServer.service.response;

import setting.SettingServer.repository.ScoreCount;

import java.util.List;

public record RatingStatisticsResponse(Double averageScore, List<ScoreCount> distribution, Long totalCount) {

}
