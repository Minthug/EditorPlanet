package setting.SettingServer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import setting.SettingServer.common.BaseTime;

@Entity
@Getter
@NoArgsConstructor
public class Tag extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int useCount;

    @Builder
    public Tag(String name) {
        this.name = name;
        this.useCount = 0;
    }

    public void incrementUseCount() {
        this.useCount++;
    }

    public void decrementUseCount() {
        this.useCount = Math.max(0, this.useCount - 1);
    }
}
