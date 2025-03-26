package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.service.ReferenceService;
import setting.SettingServer.service.request.ReferenceCreateRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/references")
@Slf4j
public class ReferenceController {

    private final ReferenceService referenceService;
    private final SecurityUtil securityUtil;

    public ResponseEntity<Void> createReference(@RequestBody @Valid ReferenceCreateRequest request) {
        log.info("참고 자료 등록 요청: {}", request);


    }
}
