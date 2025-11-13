package ymsoft.springdeveloper.com.springdeveloper.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.MonthlyPayrollDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeeklyTemplateUpdateRequest;
import ymsoft.springdeveloper.com.springdeveloper.service.ScheduleItemService;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberScheduleController {

    @Autowired
    private memberService memberService;

    @Autowired
    private ScheduleItemService scheduleItemService;

    private final ObjectMapper objectMapper; // ✅ 스프링이 모듈 등록된 ObjectMapper를 주입

    @PostMapping("/{id}/update")
    public ResponseEntity<Void> updateWeeklyTemplate(
            @PathVariable Long id,
            @RequestBody WeeklyTemplateUpdateRequest req
    ) {
        // path 의 memberId 를 우선시 (body 와 다르면 강제로 맞추거나 에러 처리)
        if (!Objects.equals(id, req.getMemberId())) {
            req.setMemberId(id);
        }

        log.info("MemberScheduleController.updateWeeklyTemplate: memberId: " + id + ", req: " + req);

        scheduleItemService.updateDailyTemplate(req);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<List<MemberDto.ScheduleRow>> getMemberSchedule(@PathVariable Long id) throws Exception {
        MemberDto member = memberService.findById(id);

        // 스케줄도 null-safe로 내려주기
        List<MemberDto.ScheduleRow> schedules =
                (member.getSchedule() != null) ? member.getSchedule() : List.of();

        String json = objectMapper.writeValueAsString(schedules);
        log.info("getMemberSchedule member {} schedule", json );
        return ResponseEntity.ok(schedules);
    }
}
