package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.dto.MemberProfileResponse;
import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.dto.MemberUpdateRequest;
import setting.SettingServer.service.MemberService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseEntity<MemberProfileResponse> findMember(@PathVariable(value = "id") Long id) {
        MemberProfileResponse member = memberService.findMember(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<MemberProfileResponse>> findAllMember() {
        List<MemberProfileResponse> members = memberService.findAllMember();
        return ResponseEntity.ok(members);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editMember(@PathVariable(value = "id") Long id,
                                        @Valid @ModelAttribute MemberUpdateRequest request) {
        MemberResponse response = memberService.editMember(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable(value = "id") Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
