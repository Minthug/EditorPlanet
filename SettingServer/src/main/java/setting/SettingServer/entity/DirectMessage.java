package setting.SettingServer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // JPA support
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recevier_id", nullable = false)
    private Member receiver;

    private String content;

    private boolean isRead;

    @CreatedDate
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    // 논리적 삭제를 위한 필드 추가
    private boolean isDeletedBySender = false;
    private boolean isDeletedByReceiver = false;


    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();

    public void markAsRead() {
        this.isRead = true;
    }


    // 발신자 삭제 메서드
    public void markDeletedBySender() {
        this.isDeletedBySender = true;
    }

    // 수신자 삭제 메서드
    public void markDeletedByReceiver() {
        this.isDeletedByReceiver = true;
    }


    public void addAttachment(MessageAttachment attachment) {
        this.attachments.add(attachment);
        attachment.setMessage(this);
    }


    public void addAttachments(List<MessageAttachment> attachments) {
        attachments.forEach(this::addAttachment);
    }

    public Long getSenderId() {
        return sender.getId();
    }

    public Long getReceiverId() {
        return receiver.getId();
    }

    public String getSenderName() {
        return sender.getName();
    }

    @PrePersist
    public void prePersist() {
        this.sentAt = LocalDateTime.now();
    }

//    public LocalDateTime getSentAt() {
//        return getCreatedAt();
//    }
}
