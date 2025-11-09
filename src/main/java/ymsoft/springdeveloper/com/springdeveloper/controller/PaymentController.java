package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.service.WorkScheduleService;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private memberService memService;

    private final ObjectMapper objectMapper; // ✅ 스프링이 모듈 등록된 ObjectMapper를 주입

    @GetMapping("")
    public String payment(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month, // ✅ 월 기준 앵커(임의의 월 내 날짜)
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,  // 🔁 호환용(주 기준 들어와도 처리)
            Model model
    ) throws Exception {

        // 0) 앵커 날짜 결정: month 우선, 없으면 week, 둘 다 없으면 오늘
        LocalDate anchor = (month != null) ? month : (week != null ? week : LocalDate.now());

        // 1) 해당 월의 시작/끝 (현지 로컬 기준)
        LocalDate startOfMonth = anchor.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth   = anchor.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        // 2) 월간 라벨/이동 URL (템플릿 키: monthRangeLabel / monthPrevUrl / monthNextUrl)
        //    라벨 예: "2025.11.01 ~ 11.30"  ← 스크립트가 여기서 시작/끝을 파싱합니다.
        DateTimeFormatter leftFmt  = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter rightFmt = DateTimeFormatter.ofPattern("MM.dd");
        model.addAttribute("monthRangeLabel",
                String.format("%s ~ %s",
                        startOfMonth.format(leftFmt),
                        endOfMonth.format(rightFmt)));

        // 3) 이전/다음 달 이동 (같은 엔드포인트로 이동하도록 수정)
        //    파라미터는 월 내 아무 날짜여도 OK (여기서는 각 월의 1일 사용)
        LocalDate prevMonthAnchor = startOfMonth.minusMonths(1);
        LocalDate nextMonthAnchor = startOfMonth.plusMonths(1);
        model.addAttribute("monthPrevUrl", "/members/showworkmonthdashboard?month=" + prevMonthAnchor);
        model.addAttribute("monthNextUrl", "/members/showworkmonthdashboard?month=" + nextMonthAnchor);

        // 4) 멤버 목록 (좌측 리스트 & 우측 표의 데이터 소스)
        List<MemberDto> members = memService.findAll();
        log.info("showworkmonthdashboard members: {}", members);
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("showworkmonthdashboard membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");

        return "members/payManagement"; // 머스태시 템플릿
    }

    @GetMapping("/payid")
    public String payid(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        log.info("/paid/ members: {}", members);
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");
        return "members/payidManagement";
    }
}
