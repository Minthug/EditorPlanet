package setting.SettingServer.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record MemberUpdateRequest(String name, String password, List<MultipartFile> imageFiles) {
}
