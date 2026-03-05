package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Slf4j
@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final memberService memService;
    private final PayrollMonthService payrollMonthService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FMT_YMD = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter FMT_MD  = DateTimeFormatter.ofPattern("MM.dd");

    private void addMonthNav(Model model, LocalDate anchor, String baseUrl) {
        LocalDate start = anchor.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end   = anchor.with(TemporalAdjusters.lastDayOfMonth());
        model.addAttribute("monthRangeLabel",
                String.format("%s ~ %s", start.format(FMT_YMD), end.format(FMT_MD)));
        model.addAttribute("monthPrevUrl", baseUrl + "?month=" + start.minusMonths(1));
        model.addAttribute("monthNextUrl", baseUrl + "?month=" + start.plusMonths(1));
    }

    @GetMapping("")
    public String payment(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            @RequestParam(required = false) Long memberId,
            Model model
    ) throws Exception {
        LocalDate anchor = month != null ? month : (week != null ? week : LocalDate.now());
        addMonthNav(model, anchor, "/payment");

        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "월간 급여 관리");
        model.addAttribute("selectedMemberId", memberId);
        return "members/payManagement";
    }

    @GetMapping("/individual/popup")
    public String paymentIndividualPopup(
            @RequestParam Long memberId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate anchor = month != null ? month : (week != null ? week : LocalDate.now());
        LocalDate start  = anchor.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end    = anchor.with(TemporalAdjusters.lastDayOfMonth());

        model.addAttribute("monthRangeLabel",
                String.format("%s ~ %s", start.format(FMT_YMD), end.format(FMT_MD)));
        model.addAttribute("monthPrevUrl",
                "/payment/individual/popup?memberId=" + memberId + "&month=" + start.minusMonths(1));
        model.addAttribute("monthNextUrl",
                "/payment/individual/popup?memberId=" + memberId + "&month=" + start.plusMonths(1));

        MemberDto member = memService.findById(memberId);
        model.addAttribute("member", member);
        model.addAttribute("memberJson", objectMapper.writeValueAsString(member));
        model.addAttribute("pageTitle", "개인 급여 관리");
        return "members/payManagementPopup";
    }

    @GetMapping("/payid")
    public String payid(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "급여 ID 관리");
        return "members/payidManagement";
    }

    @GetMapping("/payidmanager/print")
    public String paidManagerPrint(@RequestParam Long memberId, Model model) throws JsonProcessingException {
        MemberDto member = memService.findById(memberId);
        if (member == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");

        model.addAttribute("member", member);
        model.addAttribute("memberJson", objectMapper.writeValueAsString(member));
        model.addAttribute("statusLabel", Member.Status.label(member.getStatus()));
        return "members/payidManagerPrint";
    }

    @GetMapping("/payiddashboard")
    public String payidDashboard(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "급여 ID 대시보드");
        return "members/payidManagementDashboard";
    }

    @GetMapping("/paymanagerdashboard")
    public String payManagerDashboard(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "급여 관리 대시보드");
        return "members/payManagerDashboard";
    }

    @GetMapping("/paymanagerdashboard/print")
    public String payManagerDashboardPrint(@RequestParam String status, Model model) throws Exception {
        List<MemberDto> members = memService.findByStatus(Member.Status.from(status));
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("statusLabel", Member.Status.label(status));
        model.addAttribute("pageTitle", "급여 관리 대시보드 출력");
        return "members/payManagerDashboardPrint";
    }

    @GetMapping("/payroll/month/print")
    public String printMonthPayroll(
            @RequestParam Long memberId,
            @RequestParam int year,
            @RequestParam int month,
            Model model
    ) {
        MemberDto member = memService.findById(memberId);
        if (member == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd   = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<PayrollMonthResponse> pays = payrollMonthService.getMonth(memberId, year, month).stream().toList();
        PayrollMonthResponse pay = pays.isEmpty() ? null : pays.stream()
                .max(Comparator.comparing(p -> p.getUpdatedAt() != null ? p.getUpdatedAt() : p.getCreatedAt()))
                .orElse(pays.get(0));

        boolean includeWeeklyHolidayAllowance = member.getIncludeWeeklyHolidayAllowance();
        boolean applyTax = member.getApplyTax();

        Long monthTotalPay = pay != null ? pay.getMonthTotalPay() : null;
        Long taxAmount     = (applyTax && monthTotalPay != null) ? Math.round(monthTotalPay * 0.033) : null;
        Long afterTax      = (monthTotalPay != null)
                ? (taxAmount != null ? monthTotalPay - taxAmount : monthTotalPay)
                : null;

        model.addAttribute("member", member);
        model.addAttribute("pay", pay);
        model.addAttribute("hasPay", pay != null);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthStart", monthStart);
        model.addAttribute("monthEnd", monthEnd);
        model.addAttribute("includeWeeklyHolidayAllowance", includeWeeklyHolidayAllowance);
        model.addAttribute("applyTax", applyTax);
        model.addAttribute("monthWorkTimeStr",  pay != null ? formatMinutes(pay.getMonthWorkMinutes())  : null);
        model.addAttribute("monthJuhyuTimeStr", pay != null ? formatMinutes(pay.getMonthJuhyuMinutes()) : null);
        model.addAttribute("statusLabel",       pay != null ? toStatusLabel(pay.getStatus()) : "미저장");
        model.addAttribute("confirmedAtStr",    formatDateTime(pay != null ? pay.getConfirmedAt() : null));
        model.addAttribute("paidAtStr",         formatDateTime(pay != null ? pay.getPaidAt() : null));
        model.addAttribute("monthWorkPayStr",   pay != null ? formatKrw(pay.getMonthWorkPay())  : "-");
        model.addAttribute("monthJuhyuPayStr",  pay != null ? formatKrw(pay.getMonthJuhyuPay()) : "-");
        model.addAttribute("monthTotalPayStr",  monthTotalPay != null ? formatKrw(monthTotalPay) : "-");
        model.addAttribute("taxAmountStr",      taxAmount != null ? formatKrw(taxAmount) : "-");
        model.addAttribute("afterTaxStr",       afterTax  != null ? formatKrw(afterTax)  : "-");
        model.addAttribute("paydayLabel",       formatPayday(member.getPayday()));
        model.addAttribute("bankName",          member.getBankName());
        model.addAttribute("bankAccount",       member.getBankAccount());
        model.addAttribute("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        return "pay/payrollMonthPrint";
    }

    private String formatMinutes(Integer minutes) {
        if (minutes == null || minutes <= 0) return "0시간";
        int h = minutes / 60, m = minutes % 60;
        return m == 0 ? h + "시간" : h + "시간 " + m + "분";
    }

    private String toStatusLabel(String status) {
        if (status == null) return "미저장";
        return switch (status.toUpperCase()) {
            case "DRAFT"     -> "임시저장";
            case "CONFIRMED" -> "확정";
            case "PAID"      -> "지급완료";
            default          -> status;
        };
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.KOREA));
    }

    private String formatKrw(Long value) {
        return value == null ? "-" : String.format("%,d원", value);
    }

    private String formatPayday(String v) {
        if (v == null || v.isBlank()) return "미지정";
        String s = v.trim().toUpperCase();
        if (s.equals("EOM")) return "말일";
        if (s.matches("\\d+")) {
            int n = Integer.parseInt(s);
            if (n >= 1 && n <= 31) return n + "일";
        }
        return v;
    }
}
