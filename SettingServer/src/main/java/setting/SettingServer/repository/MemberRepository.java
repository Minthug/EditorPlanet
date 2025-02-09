package setting.SettingServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.ProviderType;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByRefreshToken(String refreshToken);

    Optional<Member> findByName(String name);

    Optional<Member> findByTypeAndProvider(ProviderType providerType, String providerId);
}

