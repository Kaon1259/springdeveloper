package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.service.WorkScheduleService;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/schedules")
public class ScheduleController {

    private final memberService memService;
    private final WorkScheduleService workScheduleService;
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

    /**
     * 개별 아르바이트생 주간 근무표 출력 페이지
     */
    @GetMapping("/work/weekly/print/individual")
    public String printIndividualWeekly(
            @RequestParam("memberId") Long memberId,
            @RequestParam("weekStart")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate weekStart,
            @RequestParam(value = "slot", defaultValue = "60") int slotMinutes,
            Model model
    ) {

        // 1) Member 정보 조회
        MemberDto member = memService.findById(memberId);
        if (member == null) {
            throw new IllegalArgumentException("해당 memberId의 아르바이트생이 존재하지 않습니다: " + memberId);
        }

        // 2) 주간 날짜 계산
        LocalDate weekEnd = weekStart.plusDays(6);

        log.info("printIndividualWeekly: memberId: " + memberId);
        log.info("printIndividualWeekly: weekStart: " + weekStart);
        log.info("printIndividualWeekly: weekEnd: " + weekEnd);

        // 3) 해당 멤버의 주간 근무 전체 조회
        List<WorkSchedule> weekData = workScheduleService.getWeeklyWork(memberId, weekStart, weekEnd);

        log.info("printIndividualWeekly: weekData: " + weekData.toString());

        // 4) 모델에 바인딩
        model.addAttribute("member", member);
        model.addAttribute("weekStartIso", weekStart.toString());
        model.addAttribute("weekEndIso", weekEnd.toString());
        model.addAttribute("slotMinutes", slotMinutes);
        model.addAttribute("weekData", weekData);

        // 주간 라벨: 2025.11.17 ~ 11.23 형식
        String weekLabel = String.format(
                "%d.%02d.%02d ~ %02d.%02d",
                weekStart.getYear(),
                weekStart.getMonthValue(),
                weekStart.getDayOfMonth(),
                weekEnd.getMonthValue(),
                weekEnd.getDayOfMonth()
        );
        model.addAttribute("weekLabel", weekLabel);

        return "schedules/weeklyPrintIndividual"; // 머스태치 파일
    }

}
