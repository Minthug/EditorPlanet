package setting.SettingServer.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String email;
    private String name;
    private String password;
    private String phoneNumber;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDay;
    private String userId;

    @Column(name = "image_url")
    private String imageUrl;

    private String accessToken;
    private String refreshToken;

    private String providerId;
    private String provider;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private ProviderType type;

    private boolean isDeleted;

    public Member(long id, String email, String name, String password, String phoneNumber, LocalDate birthDay, String imageUrl, UserRole role, ProviderType type, boolean isDeleted) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.birthDay = birthDay;
        this.imageUrl = imageUrl;
        this.role = role;
        this.type = type;
        this.isDeleted = isDeleted;
    }

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateProviderType(ProviderType type) {
        this.type = type;
    }

    public void hashPassword(PasswordEncoder encoder) {
        this.password = encoder.encode(this.password);
    }

    public void updateOauthInfo(String providerId, String provider) {
        this.providerId = providerId;
        this.provider = provider;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateProfileImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateMember(String name, String encryptedPassword) {
        if (name != null && !name.isEmpty()) this.name = name;
        if (password != null && !password.isEmpty()) this.password = password;
    }

    public void updateBirthDay(LocalDate localDate) {
        this.birthDay = birthDay;
    }

    public void softDeleted() {
        this.isDeleted = true;
    }
}
