package setting.SettingServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.DuplicateEmailException;
import setting.SettingServer.common.exception.LoginFailureException;
import setting.SettingServer.common.exception.UserNotFoundException;
import setting.SettingServer.common.oauth.RequestOAuthInfoService;
import setting.SettingServer.config.jwt.dto.TokenDto;
import setting.SettingServer.config.jwt.service.JwtService;
import setting.SettingServer.dto.LoginDto;
import setting.SettingServer.dto.SignUpRequest;
import setting.SettingServer.entity.JwtTokenType;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.ProviderType;
import setting.SettingServer.entity.UserRole;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.user.OAuthLoginParams;

import java.security.InvalidParameterException;
import java.util.DuplicateFormatFlagsException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final MemberService memberService;
    private final RequestOAuthInfoService oAuthInfoService;

    @Transactional
    public void signUp(SignUpRequest request) throws Exception {
        validateUniqueInfo(request);
        validateRequiredFields(request);

        Member member = Member.builder()
                .email(request.email())
                .password(request.password())
                .name(request.name())
                .role(UserRole.USER)
                .type(ProviderType.LOCAL)
                .build();

        member.hashPassword(encoder);
        memberRepository.save(member);
    }

    private void validateRequiredFields(SignUpRequest dto) {
        if (StringUtils.isEmpty(dto.email())) {
            throw new InvalidParameterException("Email is required");
        }

        if (StringUtils.isEmpty(dto.password())) {
            throw new InvalidParameterException("Password is required");
        }

        if (StringUtils.isEmpty(dto.name())) {
            throw new InvalidParameterException("Name is required");
        }
    }

    private void validateUniqueInfo(SignUpRequest dto) {
        if (memberRepository.findByEmail(dto.email()).isPresent()) {
            throw new DuplicateEmailException("Email already exists");
        }

        if (memberRepository.findByName(dto.name()).isPresent()) {
            throw new DuplicateFormatFlagsException("Name already exists");
        }
    }

    @Transactional
    public TokenDto login(LoginDto loginDto) {
        Member member = memberRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Member not found"));

        if (member.getType() == null) {
            member.updateProviderType(ProviderType.LOCAL);
            memberRepository.save(member);
        }

        if (member.getType() == ProviderType.LOCAL) {
            if (!encoder.matches(loginDto.getPassword(), member.getPassword())) {
                throw new LoginFailureException("비밀번호가 일치하지 않습니다");
            }
        }

        String accessToken = jwtService.createToken(member.getEmail(), JwtTokenType.ACCESS);
        String refreshToken = jwtService.createToken(member.getEmail(), JwtTokenType.REFRESH);

        jwtService.updateStoredToken(member.getEmail(), accessToken, true);
        jwtService.updateStoredRefreshToken(member.getEmail(), refreshToken);
        return new TokenDto(accessToken, refreshToken);
    }

    private Member findOrCreateMember(OAuthLoginParams params) {
        return memberRepository.findByEmail(params.getEmail())
                .orElseGet(() -> createMember(params));
    }

    private Member createMember(OAuthLoginParams params) {
        Member member = Member.builder()
                .email(params.getEmail())
                .name(params.getName())
                .type(params.oAuthProvider())
                .build();
        return memberRepository.save(member);
    }

    @Transactional
    public Member registerOrUpdateUser(String email, String name, String providerId, String provider) {
        ProviderType providerType = getUserTypeFromProvider(provider);

        return memberRepository.findByEmail(email)
                .map(user -> {
                    user.updateName(name);
                    user.updateProviderType(providerType);
                    user.updateOauthInfo(providerId, provider);
                    return user;
                })
                .orElseGet(() -> {
                    Member newMember = Member.builder()
                            .email(email)
                            .name(name)
                            .providerId(providerId)
                            .provider(provider)
                            .role(UserRole.USER)
                            .type(providerType)
                            .build();
                    return memberRepository.save(newMember);
                });
    }

    private ProviderType getUserTypeFromProvider(String provider) {
        switch (provider.toUpperCase()) {
            case "google":
                return ProviderType.GOOGLE;
            case "kakao":
                return ProviderType.KAKAO;
            case "naver":
                return ProviderType.NAVER;
            case "local":
                return ProviderType.LOCAL;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }
    }
}
