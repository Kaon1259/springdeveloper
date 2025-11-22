package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {

    private final memberService memService;
    private final ObjectMapper objectMapper;

    @GetMapping("/generator")
    public String openGenerator(@RequestParam("memberId") Long memberId, Model model) throws JsonProcessingException {

        log.info("openGenerator: memberId: " + memberId);

        MemberDto member = memService.findById(memberId);
        log.info("openGenerator: member: " + member);
        String memberJson = objectMapper.writeValueAsString(member);
        model.addAttribute("memberJson", memberJson);

        return "schedules/scheduleGenerator"; // 위에서 만든 템플릿 이름
    }


    @GetMapping("/work/weekly/print")
    public String weeklyPrint(@RequestParam String status,
                              @RequestParam String weekStart,
                              @RequestParam(required = false, defaultValue = "60") int slot,
                              Model model) {
        LocalDate start = LocalDate.parse(weekStart);          // yyyy-MM-dd
        LocalDate end   = start.plusDays(6);

        // 상태에 맞는 멤버 목록 조회 (이미 있으신 서비스 사용)
        List<MemberDto> members = memService.findByStatus(Member.Status.from(status));

        log.info("weeklyPrint: members: " + members.toString());

        // 머스태치에서 사용할 라벨들
        String statusLabel = Member.Status.label(status);

        String weekLabel = String.format("%d.%02d.%02d ~ %02d.%02d",
                start.getYear(), start.getMonthValue(), start.getDayOfMonth(),
                end.getMonthValue(),   end.getDayOfMonth());

        log.info("weeklyPrint: statusLabel: " + statusLabel);
        log.info("weeklyPrint: weekLabel: " + weekLabel);

        // members JSON (id, name, phone 정도만 쓰면 됨)
//        String membersJson = objectMapper.writeValueAsString(members);  // ObjectMapper 등으로 변환

        model.addAttribute("status", status);
        model.addAttribute("statusLabel", statusLabel);
        model.addAttribute("weekStartIso", start.toString());
        model.addAttribute("weekEndIso", end.toString());
        model.addAttribute("weekLabel", weekLabel);
        model.addAttribute("slotMinutes", slot);
        model.addAttribute("members", members);

        return "schedules/weeklyPrint";  // 위 머스태치 파일 경로
    }

}
