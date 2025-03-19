package setting.SettingServer.config;

import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class SecurityUtil {

    public String getCurrentMemberUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Security Context 에 인증 정보가 없습니다.");
        } else if (authentication.getName().equals("anonymousUser")) {
            throw new RuntimeException();
        }
        return authentication.getName();
    }

    public String getCurrentMemberUsernameOrNonMember() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().equals("anonymousUser")) {
            return "non-member";
        }

        return authentication.getName();
    }
}
