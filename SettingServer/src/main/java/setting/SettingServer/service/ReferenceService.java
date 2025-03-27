package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.UnauthorizedException;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.Reference;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.ReferenceRepository;
import setting.SettingServer.service.request.ReferenceCreateRequest;
import setting.SettingServer.service.request.ReferenceUpdateRequest;
import setting.SettingServer.service.response.ReferenceListResponse;
import setting.SettingServer.service.response.ReferenceResponse;

@Service
@RequiredArgsConstructor
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;

    /**
     * 참고 자료 생성
     * @param request
     * @return
     */
    public Long createReference(ReferenceCreateRequest request) {

        Long currentUserId = getCurrentUserId();

        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다"));

        Reference reference = Reference.builder()
                .title(request.title())
                .thumbnail(request.thumbnail())
                .videoUrl(request.videoUrl())
                .author(member)
                .averageRating(0.0)
                .ratingCount(0)
                .build();

        Reference savedReference = referenceRepository.save(reference);
        return savedReference.getId();
    }

    /**
     * 참고 자료 수정
     * @param referenceId
     * @param request
     */
    @Transactional
    public void updateReference(Long referenceId, ReferenceUpdateRequest request) {
        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다"));

        validateOwnership(reference);

        reference.update(
                request.title(),
                request.thumbnail(),
                request.videoUrl()
        );
    }

    /**
     * 참고 자료 삭제
     * @param referenceId
     */
    @Transactional
    public void deleteReference(Long referenceId) {

        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다"));

        validateOwnership(reference);

        referenceRepository.delete(reference);
    }

    /**
     * 참고 자료 조회
     * @param referenceId
     * @return
     */
    @Transactional
    public ReferenceResponse getReference(Long referenceId) {

        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다"));

        return ReferenceResponse.from(reference);
    }

    /**
     * 전체 참고 자료 목록 조회
     * @return
     */
    @Transactional(readOnly = true)
    public Page<ReferenceListResponse> getReferenceList(Pageable pageable) {
        return referenceRepository.findAll(pageable).map(ReferenceListResponse::from);
    }

    /**
     * 특정 회원의 참고 자료 목록 조회
     * @return
     */
    @Transactional(readOnly = true)
    public Page<ReferenceListResponse> getMemberReferenceList(Long memberId, Pageable pageable) {
        return referenceRepository.findByAuthor_IdAndIsDeletedFalse(memberId, pageable)
                .map(ReferenceListResponse::from);
    }

    /**
     * 본인 참고 자료 목록 조회
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<ReferenceListResponse> getMyReferenceList(Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return getMemberReferenceList(currentUserId, pageable);
    }


    // ================= 공통 헬퍼 메서드 =================

    /**
     * 현재 로그인한 사용자의 ID를 Long 타입으로 반환
     */
    private Long getCurrentUserId() {
        return Long.parseLong(securityUtil.getCurrentMemberUsername());
    }

    /**
     * 권한 확인: 현재 사용자와 요청 사용자가 일치하는지 검증
     */
    private void validateCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserId();
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("권한이 없습니다");
        }
    }

    /**
     *
     * @param reference
     */
    private void validateOwnership(Reference reference) {
       Long currentUserId = getCurrentUserId();
       if (!reference.getAuthor().getUserId().equals(currentUserId)) {
           throw new UnauthorizedException("게시글에 대한 권한이 없습니다");
       }
    }
}
