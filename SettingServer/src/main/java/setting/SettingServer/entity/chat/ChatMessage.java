package setting.SettingServer.entity.chat;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "ChatMessage")
@EntityListeners(value = {AuditingEntityListener.class})
@Data
@Entity
public class ChatMessage {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "roomId", insertable = false, updatable = false)
    private String roomId;
    @JoinColumn(name = "authorId", insertable = false, updatable = false)
    private String authorId;
    private String message;
    @CreatedDate
    private LocalDateTime createdAt;


}
