package setting.SettingServer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.UserNotFoundException;
import setting.SettingServer.dto.MemberDto;
import setting.SettingServer.dto.MemberResponseDto;
import setting.SettingServer.dto.MemberUpdateDto;
import setting.SettingServer.dto.ProfileDto;
import setting.SettingServer.entity.Member;
import setting.SettingServer.repository.MemberRepository;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository; // PostgreSQL
    private final RedisTemplate<String, MemberDto> redisTemplate; // Redis
    private final GcpStorageService gcpStorageService;
    private final PasswordEncoder passwordEncoder;


    @Cacheable(cacheNames = "memberCache", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public MemberDto findMember(Long id) {

        return memberRepository.findById(id)
                .map(MemberDto::toDto)
                .orElseThrow(() -> new UserNotFoundException("Member not found with id: " + id));
    }

    @Cacheable(cacheNames = "allMembersCache", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<MemberDto> findAllMember() {
        String cacheKey = "allMembers";

        List<MemberDto> cachedMembers = (List<MemberDto>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedMembers != null && !cachedMembers.isEmpty()) {
            return cachedMembers;
        }

        List<MemberDto> members = memberRepository.findAll().stream()
                .map(MemberDto::toDto)
                .collect(Collectors.toList());

        if (!members.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, (MemberDto) members, 1, TimeUnit.HOURS);

        }
            return members;
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Member not found with email: " + email));
        return ProfileDto.fromMember(member);
    }

    @Transactional
    public MemberResponseDto editMember(Long id, MemberUpdateDto updateDto) {
        log.info("Editing member with id: {} ", id);
        Member member = findMemberById(id);

        try {
            validateMemberPermission(member);
            String encryptedPassword = encryptedPasswordIfProvider(updateDto.getPassword());
            updateMemberDetails(member, updateDto, encryptedPassword);
            memberRepository.save(member);

            updateRedisMemberCache(member);

            log.info("Member updated successfully. Member url: {}", member.getImageUrl());
            return MemberResponseDto.of(member);
        } catch (Exception e) {
            log.error("Failed to update member", e);
            throw new RuntimeException("Failed to update member", e);
        }
    }

    @Transactional
    public void deleteMember(Long id) {
        Member member = findMemberById(id);
        memberRepository.delete(member);
    }

    private void updateRedisMemberCache(Member member) {
        String cacheKey = "member:" + member.getId();
        MemberDto memberDto = MemberDto.toDto(member);
        redisTemplate.opsForValue().set(cacheKey, memberDto, 1, TimeUnit.HOURS);

        updateAllMembersCache();
    }

    private void updateAllMembersCache() {
        String cacheKey = "allMembers";
        redisTemplate.delete(cacheKey);
    }

    private void updateMemberDetails(Member member, MemberUpdateDto updateDto, String encryptedPassword) {
        member.updateMember(
                updateDto.getName(),
                encryptedPassword
        );
    }

    private String encryptedPasswordIfProvider(String password) {
        if (password != null && !password.isEmpty()) {
            String encryptedPassword = passwordEncoder.encode(password);
            log.info("Password encrypted: {}", encryptedPassword);
            return encryptedPassword;
        }
        return null;
    }

    private void validateMemberPermission(Member member) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication.getPrincipal() instanceof UserDetails) ||
                !((UserDetails) authentication.getPrincipal()).getUsername().equals(member.getEmail())) {
            throw new AccessDeniedException("You don't have permission to edit this member");
        }
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Member not found with id: " + id));
    }
}
