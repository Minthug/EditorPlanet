//package setting.SettingServer.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import setting.SettingServer.common.exception.UserNotFoundException;
//import setting.SettingServer.dto.MemberDto;
//import setting.SettingServer.entity.Member;
//import setting.SettingServer.entity.ProviderType;
//import setting.SettingServer.repository.MemberRepository;
//
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class MemberServiceTest {
//
//    @Mock
//    private MemberRepository memberRepository;
//    @Mock
//    private RedisTemplate<String, MemberDto> redisTemplate;
//    @Mock
//    private ValueOperations<String, MemberDto> valueOperations;
//    @Mock
//    private GcpStorageService gcpStorageService;
//    @Mock
//    private PasswordEncoder encoder;
//    private MemberService memberService;
//
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        memberService = new MemberService(memberRepository, redisTemplate, gcpStorageService, encoder);
//    }
//
//    @Test
//    void findMember_WhenMemberInCache_ReturnsCachedMember() {
//        Long memberId = 1L;
//        String cacheKey = "member: " + memberId;
//        MemberDto cachedMember = new MemberDto(memberId, "test@example.com", "TestUser", "http://example.com/image.jpg", ProviderType.GOOGLE);
//
//        when(valueOperations.get(cacheKey)).thenReturn(cachedMember);
//
//        MemberDto result = memberService.findMember(memberId);
//
//        assertEquals(cachedMember, result);
//        verify(memberRepository, never()).findById(memberId);
//    }
//
//    @Test
//    void findMember_CheckCache_CheckRepo() {
//        Long memberId = 1L;
//        String cacheKey = "member: " + memberId;
//        Member member = new Member(memberId, "test@example.com", "TestUser", "http://example.com/image.jpg", ProviderType.GOOGLE);
//        MemberDto expectedDto = MemberDto.toDto(member);
//
//        when(valueOperations.get(cacheKey)).thenReturn(null);
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//
//        MemberDto result = memberService.findMember(memberId);
//
//        assertEquals(expectedDto, result);
//        verify(memberRepository).findById(memberId);
//        verify(valueOperations).set(eq(cacheKey), any(MemberDto.class), eq(1L), eq(TimeUnit.HOURS));
//    }
//
//    @Test
//    void findMember_ThrowException() {
//        Long memberId = 1L;
//        String cacheKey = "member: " + memberId;
//
//        when(valueOperations.get(cacheKey)).thenReturn(null);
//        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
//
//        assertThrows(UserNotFoundException.class, () -> memberService.findMember(memberId));
//        verify(memberRepository).findById(memberId);
//        verify(valueOperations, never()).set(anyString(), any(MemberDto.class), anyLong(), any(TimeUnit.class));
//
//    }
//}