package setting.SettingServer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import setting.SettingServer.entity.Reference;

import java.util.List;

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    Page<Reference> findByAuthor_Id(Long memberId, Pageable pageable);

    Page<Reference> findByAuthor_IdAndIsDeletedFalse(Long memberId, Pageable pageable);

    Page<Reference> findByAuthor_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

}
