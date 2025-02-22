package setting.SettingServer.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import setting.SettingServer.common.BaseTime;

@Entity
@Getter
@NoArgsConstructor
public class ReferenceTag extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id")
    private Reference reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Builder
    public ReferenceTag(Reference reference, Tag tag) {
        this.reference = reference;
        this.tag = tag;
    }
}
