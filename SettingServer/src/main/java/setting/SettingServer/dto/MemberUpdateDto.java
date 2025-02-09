package setting.SettingServer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberUpdateDto {

    private String name;
    private String password;
    private List<MultipartFile> imageFiles = new ArrayList<>();
}
