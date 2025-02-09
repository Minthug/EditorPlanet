package setting.SettingServer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.OauthUser;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.user.OAuth2UserInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthService {

    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public Member processOAuthPostLogin(OAuth2UserInfo oAuth2UserInfo) {
        String email = oAuth2UserInfo.getEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> createNewMember(oAuth2UserInfo));

        updateMemberIfNeeded(member, oAuth2UserInfo);
        return memberRepository.save(member);
    }

    private void updateMemberIfNeeded(Member member, OAuth2UserInfo oAuth2UserInfo) {
    }

    private Member createNewMember(OAuth2UserInfo oAuth2UserInfo) {
        return Member.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .imageUrl(oAuth2UserInfo.getProfileImageUrl())
                .type(oAuth2UserInfo.getProvider())
                .build();
    }



}


