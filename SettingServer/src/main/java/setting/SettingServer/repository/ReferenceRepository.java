package setting.SettingServer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.entity.Reference;

import java.nio.channels.FileChannel;
import java.sql.Ref;
import java.time.LocalDateTime;
import java.util.List;

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    Page<Reference> findByAuthor_Id(Long memberId, Pageable pageable);

    Page<Reference> findByAuthor_IdAndIsDeletedFalse(Long memberId, Pageable pageable);

    Page<Reference> findByAuthor_IdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Reference> findByIsDeletedFalseOrderByAverageRatingDesc(Pageable pageable);

    @Query("SELECT r from Reference r JOIN Rating rt ON rt.reference = r " +
            "WHERE rt.member.id = :userId AND r.isDeleted = false " +
            "GROUP BY r")
    Page<Reference> findByRatingsGivenByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Reference r WHERE r.createdAt >= :since AND r.isDeleted = false " +
            "ORDER BY (r.averageRating * 10 + r.viewCount / 100) DESC")
    List<Reference> findRecentPopularReferences(@Param("since")LocalDateTime since, Pageable pageable);

    @Query("SELECT r FROM Reference r JOIN r.referenceTags rt " +
            "WHERE rt.tag.id = :tagId AND r.isDeleted = false")
    Page<Reference> findByTagId(@Param("tagId") Long tagId, Pageable pageable);
}