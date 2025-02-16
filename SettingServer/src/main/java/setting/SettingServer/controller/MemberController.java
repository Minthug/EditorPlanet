package setting.SettingServer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import setting.SettingServer.dto.MemberDto;
import setting.SettingServer.dto.MemberResponse;
import setting.SettingServer.dto.MemberUpdateDto;
import setting.SettingServer.service.MemberService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/members")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> findMember(@PathVariable(value = "id") Long id) {
        MemberDto member = memberService.findMember(id);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<MemberDto>> findAllMember() {
        List<MemberDto> members = memberService.findAllMember();
        return ResponseEntity.ok(members);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editMember(@PathVariable(value = "id") Long id,
                                        @Valid @ModelAttribute MemberUpdateDto dto) {
        MemberResponse response = memberService.editMember(id, dto);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable(value = "id") Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
