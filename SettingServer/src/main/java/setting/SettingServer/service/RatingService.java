package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.DuplicateRatingException;
import setting.SettingServer.common.exception.UnauthorizedException;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.Rating;
import setting.SettingServer.entity.Reference;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.RatingRepository;
import setting.SettingServer.repository.ReferenceRepository;
import setting.SettingServer.repository.ScoreCount;
import setting.SettingServer.service.request.RatingCreateRequest;
import setting.SettingServer.service.request.RatingUpdateRequest;
import setting.SettingServer.service.response.RatingStatisticsResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ReferenceRepository referenceRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createRating(RatingCreateRequest request) {

        Reference reference = referenceRepository.findById(request.referenceId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글 입니다."));

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원 입니다"));

        if (ratingRepository.findByReferenceIdAnAndMemberId(request.referenceId(), request.memberId()).isPresent()) {
            throw new DuplicateRatingException("이미 별점을 등록한 게시글 입니다");
        }

        Rating rating = Rating.builder()
                .reference(reference)
                .member(member)
                .score(request.score())
                .build();

        Rating savedRating = ratingRepository.save(rating);
        updateReferenceAverageRating(reference);

        return savedRating.getId();
    }

    @Transactional
    public void updateRating(Long ratingId, RatingUpdateRequest request) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 별점 입니다"));

        if (!rating.getMember().getUserId().equals(request.memberId())) {
            throw new UnauthorizedException("별점을 수정할 권한이 없습니다");
        }

        rating.updateScore(request.score());
        updateReferenceAverageRating(rating.getReference());
    }

    public RatingStatisticsResponse getStatistics(Long referenceId) {
        Double averageScore = ratingRepository.calculateAverageScore(referenceId).orElse(0.0);

        List<ScoreCount> distribution = ratingRepository.getScoreDistribution(referenceId);
        long totalCount = ratingRepository.countingByReferenceId(referenceId);

        return new RatingStatisticsResponse(
                averageScore,
                distribution,
                totalCount
        );
    }

    private void updateReferenceAverageRating(Reference reference) {
        Double averageScore = ratingRepository.calculateAverageScore(reference.getId()).orElse(0.0);
        reference.updateAverageRating(averageScore);
    }
}
