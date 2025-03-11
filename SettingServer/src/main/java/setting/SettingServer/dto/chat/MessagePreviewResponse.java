package setting.SettingServer.dto.chat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record MessagePreviewResponse(Long id, String content, LocalDateTime sentAt, boolean isRead) {

    public String getPreviewContent() {
        if (content == null) return "";

        return content.length() <= 30 ? content : content.substring(0, 27) + "...";
    }

    public String getFormattedTime() {
        if (sentAt == null) return "";

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalDate messageDate = sentAt.toLocalDate();

        if (messageDate.equals(today)) {
            return DateTimeFormatter.ofPattern("a h:mm").format(sentAt);
        } else if (messageDate.equals(today.minusDays(1))) {
            return "어제";
        } else if (messageDate.getYear() == today.getYear()) {
            return DateTimeFormatter.ofPattern("M/d").format(sentAt);
        } else {
            return DateTimeFormatter.ofPattern("yy/M/d").format(sentAt);
        }
    }
}
