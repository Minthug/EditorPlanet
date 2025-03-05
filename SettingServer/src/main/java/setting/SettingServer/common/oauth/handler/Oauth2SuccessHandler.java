package setting.SettingServer.common.oauth.handler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import setting.SettingServer.config.jwt.service.JwtService;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.ProviderType;
import setting.SettingServer.entity.UserRole;
import setting.SettingServer.repository.MemberRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(oAuth2User));
    }

    private Member registerNewUser(OAuth2User oAuth2User) {
        // 기본 정보 추출
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // OAuth2 provider info check(ex. kakao, google~ )
        String provider = determineProvider(attributes);
        String providerId = determineProviderId(attributes, provider);

        // Register info Common User
        Member newMember = Member.builder()
                .email(email)
                .name(name != null ? name : "User")
                .userId(generateUserId(email))
                .role(UserRole.USER)
                .provider(provider)
                .providerId(providerId)
                .type(determineProviderType(provider))
                .imageUrl(extractProfileImageUrl(attributes, provider))
                .isDeleted(false)
                .build();

        return memberRepository.save(newMember);
    }

    private String extractProfileImageUrl(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google":
                return (String) attributes.get("picture");
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                return (String) profile.get("profile_image_url");
            case "naver":
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                return (String) response.get("profile_image");
            default:
                return null;
        }
    }

    private ProviderType determineProviderType(String provider) {
        switch (provider) {
            case "google":
                return ProviderType.GOOGLE;
            case "kakao":
                return ProviderType.KAKAO;
            case "naver":
                return ProviderType.NAVER;
            default:
                return ProviderType.LOCAL;
        }
    }

    private String determineProviderId(Map<String, Object> attributes, String provider) {
        switch (provider) {
            case "google" : return (String) attributes.get("sub");
            case "kakao" : return String.valueOf(attributes.get("id"));
            case "naver" : Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) response.get("id");
            default: return UUID.randomUUID().toString();
        }
    }

    private String determineProvider(Map<String, Object> attributes) {
        if (attributes.containsKey("sub")) return "google";
        if (attributes.containsKey("kakao_account")) return "kakao";
        if (attributes.containsKey("response")) return "naver";
        return "unknown";
    }


    private String generateUserId(String email) {
        String prefix = email.split("@")[0];
        String randomSuffix = String.valueOf(new Random().nextInt(10000));
        return prefix + "_" + randomSuffix;
    }
}
