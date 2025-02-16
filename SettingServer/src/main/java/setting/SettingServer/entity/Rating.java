package setting.SettingServer.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import setting.SettingServer.common.BaseTime;

@Entity
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "ratings", uniqueConstraints = {@UniqueConstraint(columnNames = {"reference_id", "member_id"})})
public class Rating extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id", nullable = false)
    private Reference reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Min(value = 1, message = "별점은 최소 1점 이상 입니다")
    @Max(value = 5, message = "별점은 최대 5점 입니다")
    private Integer score;

    private void validateScore(int score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("별점은 1-5 사이의 값이어야 합니다");
        }
    }

    public void updateScore(int newScore) {
        validateScore(newScore);
        this.score = newScore;
    }

}
