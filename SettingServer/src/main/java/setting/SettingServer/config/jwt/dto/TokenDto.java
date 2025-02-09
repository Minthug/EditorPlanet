package setting.SettingServer.config.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}
