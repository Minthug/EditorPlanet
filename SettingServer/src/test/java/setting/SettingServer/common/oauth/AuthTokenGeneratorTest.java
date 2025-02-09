//package setting.SettingServer.common.oauth;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Date;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//class AuthTokenGeneratorTest {
//
//    @Mock
//    private JwtTokenProvider jwtTokenProvider;
//
//    @InjectMocks
//    private AuthTokenGenerator authTokenGenerator;
//
//    private final Long testMemberId = 1L;
//
//    @Test
//    @DisplayName("토큰 생성 테스트")
//    void testGenerate() {
//        // given
//        String testAccessToken = "test_access_token";
//        String testRefreshToken = "test_refresh_token";
//        when(jwtTokenProvider.generate(eq(testMemberId.toString()), any(Date.class)))
//                .thenReturn(testAccessToken)
//                .thenReturn(testRefreshToken);
//
//        // when
//        AuthTokens tokens = authTokenGenerator.generate(testMemberId);
//
//        // then
//        assertThat(tokens.getAccessToken()).isEqualTo(testAccessToken);
//        assertThat(tokens.getRefreshToken()).isEqualTo(testRefreshToken);
//        assertThat(tokens.getGrantType()).isEqualTo("Bearer ");
//        assertThat(tokens.getExpiresIn()).isEqualTo(3600L); // 1 hour in seconds
//    }
//
//    @Test
//    @DisplayName("멤버 ID 추출 테스트")
//    void testExtractMemberId() {
//        // given
//        String testAccessToken = "test_access_token";
//        when(jwtTokenProvider.extractSubject(testAccessToken)).thenReturn(testMemberId.toString());
//
//        // when
//        Long extractedMemberId = authTokenGenerator.extractMemberId(testAccessToken);
//
//        // then
//        assertThat(extractedMemberId).isEqualTo(testMemberId);
//    }
//}