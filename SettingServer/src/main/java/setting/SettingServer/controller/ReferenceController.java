package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.service.ReferenceService;
import setting.SettingServer.service.request.ReferenceCreateRequest;
import setting.SettingServer.service.request.ReferenceUpdateRequest;
import setting.SettingServer.service.response.ReferenceDetailResponse;
import setting.SettingServer.service.response.ReferenceListResponse;
import setting.SettingServer.service.response.ReferenceResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/references")
@Slf4j
public class ReferenceController {

    private final ReferenceService referenceService;
    private final SecurityUtil securityUtil;

    /**
     * 참고 자료 등록
     * @param request
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> createReference(@RequestBody @Valid ReferenceCreateRequest request) {
        log.info("참고 자료 등록 요청: {}", request);

        Long referenceId = referenceService.createReference(request);
        URI location = URI.create("/v1/references/" + referenceId);

        return ResponseEntity.created(location).build();
    }

    /**
     * 참고 자료 수정
     * @param referenceId
     * @param request
     * @return
     */
    @PatchMapping("/{referenceId}")
    public ResponseEntity<Void> updateReference(@PathVariable Long referenceId,
                                                @Valid @RequestBody ReferenceUpdateRequest request) {

        log.info("참고 자료 수정 요청: referenceId={}, request={}", referenceId, request);

        referenceService.updateReference(referenceId, request);
        return ResponseEntity.noContent().build();
    }
    /**
     * 참고 자료 삭제
     * @param referenceId
     * @return
     */
    @DeleteMapping("/{referenceId}")
    public ResponseEntity<Void> deleteReference(@PathVariable Long referenceId) {
        log.info("참고 자료 삭제 요청: referenceId={}", referenceId);

        referenceService.deleteReference(referenceId);

        return ResponseEntity.noContent().build();
    }

    /**
     * 참고 자료 조회 (V2 별점 추가)
     * @param referenceId
     * @return
     */
    @GetMapping("/{referenceId}")
    public ResponseEntity<ReferenceDetailResponse> getReference(@PathVariable Long referenceId) {
        log.info("참고 자료 상세 조회 요청: referenceId={}", referenceId);

        ReferenceDetailResponse reference = referenceService.getReferenceWithRating(referenceId);
        return ResponseEntity.ok(reference);
    }

    @GetMapping("/popular")
    public ResponseEntity<Page<ReferenceListResponse>> getPopularReferences(@PageableDefault(size = 20) Pageable pageable) {

        Page<ReferenceListResponse> references = referenceService.getPopularReferences(pageable);
        return ResponseEntity.ok(references);
    }

    @GetMapping("/trending")
    public ResponseEntity<List<ReferenceListResponse>> getTrendingReferences() {
        List<ReferenceListResponse> references = referenceService.getRecentPopularReferences();
        return ResponseEntity.ok(references);
    }

//    @GetMapping("/tags/{tagId}")
//    public ResponseEntity<Page<ReferenceListResponse>> getReferencesByTag(@PathVariable Long tagId,
//                                                                          @PageableDefault(size = 20) Pageable pageable) {
//
//    }

    /**
     * 참고 자료 목록 조회
     * @param pageable
     * @return
     */
    @GetMapping
    public ResponseEntity<Page<ReferenceListResponse>> getReferenceList(@PageableDefault(size = 20) Pageable pageable) {
        log.info("참고 자료 목록 조회 요청: pageable={}", pageable);

        Page<ReferenceListResponse> responses = referenceService.getReferenceList(pageable);
        return ResponseEntity.ok(responses);
    }

    /**
     * 회원별 참고 자료 조회 요청
     * @param memberId
     * @param pageable
     * @return
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<Page<ReferenceListResponse>> getMemberReferenceList(@PathVariable Long memberId,
                                                                              @PageableDefault(size = 20) Pageable pageable) {
        log.info("회원별 참고 자료 목록 조회 요청: memberId={}, pageable={}", memberId, pageable);

        Page<ReferenceListResponse> references = referenceService.getMemberReferenceList(memberId, pageable);

        return ResponseEntity.ok(references);
    }

    /**
     * 내가 등록한 참고 자료 조회 요청
     * @param pageable
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<Page<ReferenceListResponse>> getMyReferenceList(@PageableDefault(size = 20) Pageable pageable) {
        log.info("내 참고 자료 목록 조회 요청: pageable={}", pageable);

        Page<ReferenceListResponse> references = referenceService.getMyReferenceList(pageable);
        return ResponseEntity.ok(references);
    }
}
