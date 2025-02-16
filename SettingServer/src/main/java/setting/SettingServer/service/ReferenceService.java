package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.common.exception.UnauthorizedException;
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


    public Long createReference(ReferenceCreateRequest request) {
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

    @Transactional
    public void updateReference(Long referenceId, ReferenceUpdateRequest request) {
        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다"));

        if (!reference.getAuthor().getUserId().equals(request.memberId())) {
            throw new UnauthorizedException("게시글 수정 권한이 없습니다");
        }

        reference.update(
                request.title(),
                request.thumbnail(),
                request.videoUrl()
        );
    }

    @Transactional
    public void deleteReference(Long referenceId, Long memberId) {

        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다"));

        if (!reference.getAuthor().getUserId().equals(memberId)) {
            throw new UnauthorizedException("게시글 수정 권한이 없습니다");
        }

        referenceRepository.delete(reference);
    }

    public ReferenceResponse getReference(Long referenceId) {

        Reference reference = referenceRepository.findById(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 게시글입니다"));

        return ReferenceResponse.from(reference);
    }

    public Page<ReferenceListResponse> getReferenceList(Pageable pageable) {
        return referenceRepository.findAll(pageable).map(ReferenceListResponse::from);
    }

    public Page<ReferenceListResponse> getMemberReferenceList(Long memberId, Pageable pageable) {
        return referenceRepository.findByAuthor_IdAndIsDeletedFalse(memberId, pageable)
                .map(ReferenceListResponse::from);
    }
}
