package setting.SettingServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameContaining(String keyword);

    List<Tag> findTop10ByOrderByUseCountDesc();

}
