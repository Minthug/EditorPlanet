package setting.SettingServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import setting.SettingServer.entity.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByReferenceId(Long referenceId);

    Optional<Rating> findByReferenceIdAnAndMemberId(Long referenceId, Long memberId);

    @Query("select AVG(r.score) from Rating r WHERE r.id = :referenceId")
    Optional<Double> calculateAverageScore(@Param("referenceId") Long referenceId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.id = :referenceId")
    long countingByReferenceId(@Param("referenceId") Long referenceId);

    @Query("SELECT r.score as score, COUNT(r) as count " +
            "FROM Rating r " +
            "WHERE r.id = :referenceId " +
            "GROUP BY r.score")
    List<ScoreCount> getScoreDistribution(@Param("referenceId") Long referenceId);
}
