package setting.SettingServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.OauthUser;

public interface OauthUserRepository extends JpaRepository<OauthUser, Long> {
}
