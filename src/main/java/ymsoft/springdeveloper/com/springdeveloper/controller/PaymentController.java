package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.PayrollMonthResponse;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.service.PayrollMonthService;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    @Autowired
    private memberService memService;

    @Autowired
    private PayrollMonthService payrollMonthService;

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

    @GetMapping("/individual/popup")
    public String paymentIndividualPopup(
            @RequestParam Long memberId,
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
        MemberDto member = memService.findById(memberId);
        log.info("paymentIndividualPopup members: {}", member);
        model.addAttribute("members", member);

        String memberJson = objectMapper.writeValueAsString(member);
        log.info("paymentIndividualPopup membersJson: {}", memberJson);
        model.addAttribute("memberJson", memberJson);

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");

        return "members/payManagementPopup"; // 머스태시 템플릿
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

    @GetMapping("/payidmanager/print")
    public String paidManagerPrint(@RequestParam("memberId") Long memberId,
                                   Model model) throws JsonProcessingException {

        MemberDto member = memService.findById(memberId);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }

        // 상태 코드 → 한글 라벨 맵핑
        String statusLabel = Member.Status.label(member.getStatus());

        String memberJson = objectMapper.writeValueAsString(member);

        model.addAttribute("member", member);
        model.addAttribute("memberJson", memberJson);
        model.addAttribute("statusLabel", statusLabel);

        return "members/payidManagerPrint"; // templates/payment/paidManagerPrint.mustache
    }

    @GetMapping("/payiddashboard")
    public String payidDashboard(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        log.info("/paid/ members: {}", members);
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");
        return "members/payidManagementDashboard";
    }

    @GetMapping("/paymanagerdashboard")
    public String payManagerDashboard(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        log.info("/paymanagerdashboard/ members: {}", members);
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");
        return "members/payManagerDashboard";
    }

    @GetMapping("/paymanagerdashboard/print")
    public String payManagerDashboardPrint(@RequestParam String status,
                                           Model model) throws Exception {
        List<MemberDto> members = memService.findByStatus(Member.Status.from(status));
        log.info("/paymanagerdashboard/print: {}", members);
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);
        // 3) 상태 라벨 (머스태치에서 {{statusLabel}} 로 사용)
        model.addAttribute("statusLabel", Member.Status.label(status));

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");
        return "members/payManagerDashboardPrint";
    }

    @GetMapping("/payroll/month/print")
    public String printMonthPayroll(
            @RequestParam Long memberId,
            @RequestParam int year,
            @RequestParam int month,
            Model model
    ) {
        // 1) 멤버 정보
        MemberDto member = memService.findById(memberId);

        // 2) 해당 월의 시작/끝
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd   = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        // 3) 해당 월 급여(월 단위) 데이터 조회
        //    - 보통 1건이라고 가정, 여러 건이면 가장 최근(updatedAt 기준) 1건 사용
        List<PayrollMonthResponse> pays =
                payrollMonthService.getMonth(memberId, year, month).stream().toList();

        log.info("/pay/payroll/month/print memberId={}, year={}, month={}, pays={}",
                memberId, year, month, pays);

        PayrollMonthResponse pay = null;
        if (!pays.isEmpty()) {
            pay = pays.stream()
                    .max(Comparator.comparing(p -> p.getUpdatedAt() != null ? p.getUpdatedAt() : p.getCreatedAt()))
                    .orElse(pays.get(0));
        }

        // 주휴수당/세금 플래그
        boolean includeWeeklyHolidayAllowance = member.getIncludeWeeklyHolidayAllowance();
        boolean applyTax = member.getApplyTax();

        // 4) 월 합계/세금/세후 금액 계산
        Long monthTotalPay = (pay != null ? pay.getMonthTotalPay() : null);
        Long taxAmount = null;
        Long afterTax = null;

        if (applyTax && monthTotalPay != null) {
            taxAmount = Math.round(monthTotalPay * 0.033);
            afterTax = monthTotalPay - taxAmount;
        } else if (monthTotalPay != null) {
            // 세금 미적용이면 세후 = 세전
            afterTax = monthTotalPay;
        }

        // 5) 분 → "시/분" 문자열로 변환
        String monthWorkTimeStr = null;
        String monthJuhyuTimeStr = null;
        if (pay != null) {
            monthWorkTimeStr = formatMinutes(pay.getMonthWorkMinutes());
            monthJuhyuTimeStr = formatMinutes(pay.getMonthJuhyuMinutes());
        }

        // 6) 상태/날짜 문자열
        String statusLabel = (pay != null ? toStatusLabel(pay.getStatus()) : "미저장");
        String confirmedAtStr = formatDateTime(pay != null ? pay.getConfirmedAt() : null);
        String paidAtStr      = formatDateTime(pay != null ? pay.getPaidAt() : null);

        // 7) 통화 포맷(원 단위)
        String monthWorkPayStr   = (pay != null ? formatKrw(pay.getMonthWorkPay()) : "-");
        String monthJuhyuPayStr  = (pay != null ? formatKrw(pay.getMonthJuhyuPay()) : "-");
        String monthTotalPayStr  = (monthTotalPay != null ? formatKrw(monthTotalPay) : "-");
        String taxAmountStr      = (taxAmount != null ? formatKrw(taxAmount) : "-");
        String afterTaxStr       = (afterTax != null ? formatKrw(afterTax) : "-");

        // 8) 급여지급일(멤버 설정값)
        String paydayLabel = formatPayday(member.getPayday());

        // 9) 모델 바인딩
        model.addAttribute("member", member);
        model.addAttribute("pay", pay);
        model.addAttribute("hasPay", pay != null);

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthStart", monthStart);
        model.addAttribute("monthEnd", monthEnd);

        model.addAttribute("includeWeeklyHolidayAllowance", includeWeeklyHolidayAllowance);
        model.addAttribute("applyTax", applyTax);

        model.addAttribute("monthWorkTimeStr", monthWorkTimeStr);
        model.addAttribute("monthJuhyuTimeStr", monthJuhyuTimeStr);

        model.addAttribute("statusLabel", statusLabel);
        model.addAttribute("confirmedAtStr", confirmedAtStr);
        model.addAttribute("paidAtStr", paidAtStr);

        model.addAttribute("monthWorkPayStr", monthWorkPayStr);
        model.addAttribute("monthJuhyuPayStr", monthJuhyuPayStr);
        model.addAttribute("monthTotalPayStr", monthTotalPayStr);
        model.addAttribute("taxAmountStr", taxAmountStr);
        model.addAttribute("afterTaxStr", afterTaxStr);

        model.addAttribute("paydayLabel", paydayLabel);

        // 은행 정보
        model.addAttribute("bankName", member.getBankName());
        model.addAttribute("bankAccount", member.getBankAccount());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // ✅ 여기 때문에 에러났었음: now 추가
        model.addAttribute("now", LocalDateTime.now().format(dtf));

        // PDF로 뽑기 좋은 단순 출력용 뷰
        // src/main/resources/templates/pay/payrollMonthPrint.mustache
        return "pay/payrollMonthPrint";
    }

    /** 분 -> "n시간 m분" */
    private String formatMinutes(Integer minutes) {
        if (minutes == null || minutes <= 0) return "0시간";
        int h = minutes / 60;
        int m = minutes % 60;
        if (m == 0) return h + "시간";
        return h + "시간 " + m + "분";
    }

    /** 상태 -> 한글 라벨 */
    private String toStatusLabel(String status) {
        if (status == null) return "미저장";
        return switch (status.toUpperCase()) {
            case "DRAFT"     -> "임시저장";
            case "CONFIRMED" -> "확정";
            case "PAID"      -> "지급완료";
            default          -> status;
        };
    }

    /** LocalDateTime -> "yyyy-MM-dd HH:mm" */
    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return null;
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREA);
        return dt.format(f);
    }

    /** Long 원화 포맷 (null -> "-") */
    private String formatKrw(Long value) {
        if (value == null) return "-";
        return String.format("%,d원", value);
    }

    /** 지급일 포맷(EOM 또는 숫자) */
    private String formatPayday(String v) {
        if (v == null || v.trim().isEmpty()) return "미지정";
        String s = v.trim().toUpperCase();
        if (s.equals("EOM")) return "말일";
        if (s.matches("\\d+")) {
            int n = Integer.parseInt(s);
            if (n >= 1 && n <= 31) return n + "일";
        }
        return v;
    }
}
