package setting.SettingServer.config.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import setting.SettingServer.common.oauth.AuthTokenGenerator;
import setting.SettingServer.common.oauth.AuthTokens;
import setting.SettingServer.common.oauth.RequestOAuthInfoService;
import setting.SettingServer.entity.Member;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.user.OAuth2UserInfo;
import setting.SettingServer.user.OAuthLoginParams;

@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final AuthTokenGenerator authTokenGenerator;
    private final RequestOAuthInfoService requestOAuthInfoService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. username: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().getKey())
                .build();
    }

    public AuthTokens login(OAuthLoginParams params) {
        OAuth2UserInfo oAuth2UserInfo = requestOAuthInfoService.request(params);
        Long memberId = findOrCreateMember(oAuth2UserInfo);
        return authTokenGenerator.generate(memberId);
    }

    private Long findOrCreateMember(OAuth2UserInfo oAuth2UserInfo) {
        return memberRepository.findByEmail(oAuth2UserInfo.getEmail())
                .map(Member::getId)
                .orElseGet(() -> newMember(oAuth2UserInfo));
    }

    private Long newMember(OAuth2UserInfo oAuth2UserInfo) {
        Member member = Member.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .type(oAuth2UserInfo.getProvider())
                .build();
        return memberRepository.save(member).getId();
    }
}
