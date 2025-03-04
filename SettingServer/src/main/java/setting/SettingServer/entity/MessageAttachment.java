package setting.SettingServer.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class MessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private String fileName;
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private DirectMessage message;

    private String contentType;

    public void setMessage(DirectMessage message) {
        this.message = message;
    }
}
