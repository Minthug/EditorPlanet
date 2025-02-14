package setting.SettingServer.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.entity.Member;
import setting.SettingServer.entity.Reference;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.ReferenceRepository;
import setting.SettingServer.service.response.ReferenceCreateResponse;

@Service
@RequiredArgsConstructor
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final MemberRepository memberRepository;


    public Long createReference(ReferenceCreateResponse response) {
        Member member = memberRepository.findById(response.memberId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다"));

        Reference reference = Reference.builder()
                .title(response.title())
                .thumbnail(response.thumbnail())
                .videoUrl(response.videoUrl())
                .author(member)
                .averageRating(0.0)
                .ratingCount(0)
                .build();

        Reference savedReference = referenceRepository.save(reference);
        return savedReference.getId();
    }

    @Transactional
    public void updateReference(Long referenceId, ReferenceUpdateResponse response) {

    }
}
