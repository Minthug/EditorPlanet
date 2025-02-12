package setting.SettingServer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import setting.SettingServer.repository.MemberRepository;
import setting.SettingServer.repository.ReferenceRepository;

@Service
@RequiredArgsConstructor
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final MemberRepository memberRepository;


    public Long createReference() {

    }
}
