package setting.SettingServer.config.jwt.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_FILTER_REQUEST_URL = "/login";
    private static final String HTTP_METHOD = "POST";
    private static final String CONTENT_TYPE = "application/json";
    private static final String USERNAME_KEY = "email";
    private static final String PASSWORD_KEY = "password";
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_FILTER_REQUEST_URL, HTTP_METHOD);

    private final ObjectMapper objectMapper;

    public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

        // 1. HTTP Method check
        if (!request.getMethod().equals(HTTP_METHOD)) {
            throw new AuthenticationServiceException(
                    "Authentication method not supported: " + request.getMethod());
        }

        // 2. Content-Type check
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains(CONTENT_TYPE)) {
            throw new AuthenticationServiceException(
                    "Authentication Content-Type not supported: " + contentType);
        }

        try {
            // 3. Request Content Read
            String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            log.debug("Attempting authentication with request body");

            if (!StringUtils.hasText(messageBody)) {
                throw new AuthenticationServiceException("Empty request body");
            }

            // 4. JSON Parsing
            Map<String, String> usernamePasswordMap;
            try {
                usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);
            } catch (JsonProcessingException e) {
                throw new AuthenticationServiceException("Failed to parse authentication request body", e);
            }

            // 5. extraction email && password (add null check)
            String email = usernamePasswordMap.get(USERNAME_KEY);
            String password = usernamePasswordMap.get(PASSWORD_KEY);

            if (!StringUtils.hasText(email)) {
                throw new AuthenticationServiceException("Email is required");
            }

            if (!StringUtils.hasText(password)) {
                throw new AuthenticationServiceException("Password is required");
            }

            // 6. Create AuthenticationToken, Validate
            log.info("Authentication attempt for user: {}", email);
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(email, password);

            setDetails(request, authRequest);

            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            log.error("Failed for process authentication request", e);
            throw new AuthenticationServiceException("Authentication failed due to Server error", e);
        }
    }

    /**
     * Request Validate add Details
     */
    protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
