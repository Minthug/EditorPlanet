package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.service.ReferenceService;
import setting.SettingServer.service.request.ReferenceCreateRequest;
import setting.SettingServer.service.request.ReferenceUpdateRequest;
import setting.SettingServer.service.response.ReferenceListResponse;
import setting.SettingServer.service.response.ReferenceResponse;

import java.net.URI;

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

    @DeleteMapping("/{referenceId}")
    public ResponseEntity<Void> deleteReference() {

        return ;
    }

    @GetMapping
    public ResponseEntity<ReferenceResponse> getReference() {

    }

    @GetMapping
    public ResponseEntity<Page<ReferenceListResponse>> getReferenceList() {

    }


}
