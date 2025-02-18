package setting.SettingServer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.UserRole;

public record LoginRequest(@NotBlank(message = "이메일은 필수 입력값 입니다") @Email(message = "올바르지 않은 이메일 형식 입니다") String email,
                           @NotBlank(message = "비밀번호는 필수 입력값 입니다") String password) {

    public Member toMember(PasswordEncoder encoder) {
        return Member.builder()
                .email(email)
                .password(password)
                .role(UserRole.USER)
                .build();
    }

    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
    }
}
