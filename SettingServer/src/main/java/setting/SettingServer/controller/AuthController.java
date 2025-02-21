package setting.SettingServer.controller;

import com.google.api.gax.rpc.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.common.exception.DuplicateEmailException;
import setting.SettingServer.common.exception.LoginFailureException;
import setting.SettingServer.common.oauth.AuthTokens;
import setting.SettingServer.config.jwt.service.LoginService;
import setting.SettingServer.dto.LoginRequest;
import setting.SettingServer.dto.ProfileResponse;
import setting.SettingServer.dto.SignUpRequest;
import setting.SettingServer.service.AuthService;
import setting.SettingServer.service.MemberService;
import setting.SettingServer.user.params.GoogleLoginParams;
import setting.SettingServer.user.params.KakaoLoginParams;
import setting.SettingServer.user.params.NaverLoginParams;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final AuthService authService;
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity sign_up(@RequestBody SignUpRequest request) throws Exception {
        if (request.email() == null || request.email().trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "이메일 주소 입력"));
        }
        if (request.password() == null || request.password().trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "비밀번호 입력"));
        }
        if (request.name() == null || request.name().trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "이름 입력"));
        }

        try {
            authService.signUp(request);
            return ResponseEntity.ok().body(Map.of("Message", "회원가입 완료"));
        } catch (DuplicateEmailException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "중복된 이메일"));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity sign_in(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (LoginFailureException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("Error", "Failed Login", "Message", "비밀번호가 일치하지 않습니다."));
        } catch (NotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("Error", "Failed Login", "Message", "존재하지 않는 이메일 입니다."));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        ProfileResponse profileDto = memberService.getProfile(email);
        return ResponseEntity.ok(profileDto);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthTokens> loginGoogle(@RequestBody GoogleLoginParams params) {
        return ResponseEntity.ok(loginService.login(params));
    }

    @PostMapping("/kakao")
    public ResponseEntity<AuthTokens> loginKakao(@RequestBody KakaoLoginParams params) {
        return ResponseEntity.ok(loginService.login(params));
    }

    @PostMapping("/naver")
    public ResponseEntity<AuthTokens> loginNaver(@RequestBody NaverLoginParams params) {
        return ResponseEntity.ok(loginService.login(params));
    }

}
