package setting.SettingServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.Reference;

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

}
