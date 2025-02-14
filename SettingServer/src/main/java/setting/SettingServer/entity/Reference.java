package setting.SettingServer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import setting.SettingServer.common.BaseTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Reference extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String thumbnail;

    private String videoUrl;

    private int viewCount;

    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member author;

    private double averageRating;

    private int ratingCount;

    public void update(String title, String thumbnail, String videoUrl) {
        this.title = title;
        this.thumbnail = thumbnail;
        this.videoUrl = videoUrl;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    // SoftDelete
    public void delete() {
        this.isDeleted = true;
    }

    public void updateAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void updateRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
}
