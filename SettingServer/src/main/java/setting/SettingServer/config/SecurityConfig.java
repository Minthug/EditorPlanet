package setting.SettingServer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import setting.SettingServer.common.oauth.handler.OauthLoginFailureHandler;
import setting.SettingServer.common.oauth.handler.OauthLoginSuccessHandler;
import setting.SettingServer.common.oauth.service.CustomOAuth2UserService;
import setting.SettingServer.config.jwt.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import setting.SettingServer.config.jwt.filter.JwtAuthenticationProcessingFilter;
import setting.SettingServer.config.jwt.handler.LoginFailureHandler;
import setting.SettingServer.config.jwt.handler.LoginSuccessHandler;
import setting.SettingServer.config.jwt.service.JwtService;
import setting.SettingServer.config.jwt.service.LoginService;
import setting.SettingServer.repository.MemberRepository;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final LoginService loginService;
    private final OauthLoginSuccessHandler oauthLoginSuccessHandler;
    private final OauthLoginFailureHandler oauthLoginFailureHandler;
    private final CustomOAuth2UserService customOauthUserService;

    private static final String[] PUBLIC_URLS = {
            "/v1/auth/**", "/v1/upload/**", "/v1/boards","/oauth2/authorization/**", "/",
            "/css/**", "/images/**", "/js/**", "/h2-console/**", "/favicon.ico", "/error", "/v1/oauth/**",
            "/v1/chat/**", "/refresh"
        };

    private static final String[] USER_ADMIN_AUTHORITIES = {"USER", "ADMIN"};

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(configurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .headers(headers -> headers.frameOptions().disable())
                .authorizeHttpRequests(auth -> configureAuthorizeHttpRequests(auth))
                .oauth2Login(oauth2 -> configureOauth2Login(oauth2))
                .addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class)
                .addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter = new JwtAuthenticationProcessingFilter(jwtService, memberRepository);
        return jwtAuthenticationProcessingFilter;
    }

    @Bean
    public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter() {
        CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter =
                new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordAuthenticationFilter;
    }

    private void configureOauth2Login(OAuth2LoginConfigurer<HttpSecurity> oauth2) {
        oauth2
                .authorizationEndpoint(authorizationEndPoint ->
                        authorizationEndPoint.baseUri("/v1/auth/oauth2/authorization"))
                .redirectionEndpoint(redirectionEndPoint ->
                        redirectionEndPoint.baseUri("/v1/auth/oauth2/authorization"))
                .successHandler(oauthLoginSuccessHandler)
                .failureHandler(oauthLoginFailureHandler)
                .userInfoEndpoint(userInfo -> userInfo.userService(customOauthUserService));
    }

    private void configureAuthorizeHttpRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/members/", "/v1/members/{id}", "/v1/members").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/v1/members/{id}").hasAnyAuthority(USER_ADMIN_AUTHORITIES)
                .requestMatchers(HttpMethod.POST, "/refresh").permitAll();

        configureUserAdminRequests(auth);
        auth.anyRequest().authenticated();
    }

    private void configureUserAdminRequests(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
                .requestMatchers(HttpMethod.GET, "/v1/members/profile").hasAnyAuthority(USER_ADMIN_AUTHORITIES)
                .requestMatchers(HttpMethod.DELETE, "/v1/members/**").hasAnyAuthority(USER_ADMIN_AUTHORITIES);
    }

    @Bean
    public CorsConfigurationSource configurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "RefreshToken"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtService, memberRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
