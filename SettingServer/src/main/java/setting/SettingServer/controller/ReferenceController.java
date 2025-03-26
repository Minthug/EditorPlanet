package setting.SettingServer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import setting.SettingServer.config.SecurityUtil;
import setting.SettingServer.service.ReferenceService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/references")
@Slf4j
public class ReferenceController {

    private final ReferenceService referenceService;
    private final SecurityUtil securityUtil;

}
