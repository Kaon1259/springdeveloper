package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberRealtimeDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.service.WorkScheduleService;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final memberService memService;
    private final WorkScheduleService workScheduleService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FMT_YMD = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter FMT_MD  = DateTimeFormatter.ofPattern("MM.dd");

    // ────────────────────────────────────────
    //  공통: 주간 네비게이션 모델 바인딩
    // ────────────────────────────────────────

    private void addWeekNav(Model model, LocalDate base, String baseUrl) {
        LocalDate start = base.with(DayOfWeek.MONDAY);
        LocalDate end   = start.plusDays(6);
        model.addAttribute("weekRangeLabel",
                String.format("%s ~ %s", start.format(FMT_YMD), end.format(FMT_MD)));
        model.addAttribute("weekPrevUrl", baseUrl + "?week=" + start.minusWeeks(1));
        model.addAttribute("weekNextUrl", baseUrl + "?week=" + start.plusWeeks(1));
    }

    private void addMonthNav(Model model, LocalDate anchor, String baseUrl) {
        LocalDate startOfMonth = anchor.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth   = anchor.with(TemporalAdjusters.lastDayOfMonth());
        model.addAttribute("monthRangeLabel",
                String.format("%s ~ %s", startOfMonth.format(FMT_YMD), endOfMonth.format(FMT_MD)));
        model.addAttribute("monthPrevUrl", baseUrl + "?month=" + startOfMonth.minusMonths(1));
        model.addAttribute("monthNextUrl", baseUrl + "?month=" + startOfMonth.plusMonths(1));
    }

    private String toDayKey(LocalDate date) {
        return date.getDayOfWeek().name().substring(0, 3);
    }

    // ────────────────────────────────────────
    //  근무자 목록 (주간 네비)
    // ────────────────────────────────────────

    @GetMapping("/members")
    public String memberList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate base = week != null ? week : LocalDate.now();
        addWeekNav(model, base, "/members");

        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        return "members/list";
    }

    // ────────────────────────────────────────
    //  실시간 대시보드
    // ────────────────────────────────────────

    @GetMapping("/members/realtimedashboard")
    public String realtimedashboard(Model model) throws Exception {
        LocalDate today = LocalDate.now();
        List<MemberDto> members = memService.findByStatus(Member.Status.WORKING);
        List<WorkSchedule> todaySchedules = workScheduleService.findByWorkDateWithMember(today);

        Map<Long, List<WorkSchedule>> wsByMember = todaySchedules.stream()
                .collect(Collectors.groupingBy(WorkSchedule::getMemberId));

        String dayKey = toDayKey(today);
        List<MemberRealtimeDto> viewMembers = members.stream()
                .map(m -> MemberRealtimeDto.from(m, wsByMember.getOrDefault(m.getId(), List.of()), dayKey))
                .collect(Collectors.toList());

        model.addAttribute("members", viewMembers);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(viewMembers));
        return "members/realtimedashboard";
    }

    // ────────────────────────────────────────
    //  근무시간 허브 / 대시보드
    // ────────────────────────────────────────

    @GetMapping("/members/showworktimehub")
    public String showWorkTimeHub() {
        return "members/showWorkTimeHub";
    }

    @GetMapping("/members/showworktimedashboard")
    public String showWorkTimeDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate base = week != null ? week : LocalDate.now();
        addWeekNav(model, base, "/members/showworktimedashboard");

        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "주간 실 근무시간 대시보드");
        return "members/showWorktimeDashboard";
    }

    @GetMapping("/members/showworktimedashboardall")
    public String showWorkTimeDashboardAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate base = week != null ? week : LocalDate.now();
        addWeekNav(model, base, "/members/showworktimedashboardall");

        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "전체 주간 실 근무시간 대시보드");
        return "members/showWorktimeDashboardAll";
    }

    @GetMapping("/members/showworktimedashboardpopup")
    public String showWorkTimeDashboardPopup(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) throws Exception {
        LocalDate base = startDate != null ? startDate : LocalDate.now();
        LocalDate start = base.with(DayOfWeek.MONDAY);
        LocalDate end   = start.plusDays(6);

        model.addAttribute("weekRangeLabel",
                String.format("%s ~ %s", start.format(FMT_YMD), end.format(FMT_MD)));
        model.addAttribute("weekPrevUrl",
                "/members/showworktimedashboardpopup?memberId=" + memberId
                        + "&startDate=" + start.minusWeeks(1)
                        + "&endDate=" + start.minusWeeks(1).plusDays(6));
        model.addAttribute("weekNextUrl",
                "/members/showworktimedashboardpopup?memberId=" + memberId
                        + "&startDate=" + start.plusWeeks(1)
                        + "&endDate=" + start.plusWeeks(1).plusDays(6));

        List<MemberDto> members = List.of(memService.findById(memberId));
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "주간 실 근무시간 (팝업)");
        return "members/showWorktimeDashboardPopup";
    }

    // ────────────────────────────────────────
    //  월간 대시보드
    // ────────────────────────────────────────

    @GetMapping("/members/showworkmonthdashboard")
    public String showWorkMonthDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate anchor = month != null ? month : (week != null ? week : LocalDate.now());
        addMonthNav(model, anchor, "/members/showworkmonthdashboard");

        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "월간 실 근무시간 대시보드");
        return "members/showWorkMonthDashboard";
    }

    @GetMapping("/members/showworkMonthdashboardall")
    public String showWorkMonthDashboardAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate base = week != null ? week : LocalDate.now();
        addWeekNav(model, base, "/members/showworkMonthdashboardall");

        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "전체 월간 실 근무시간 대시보드");
        return "members/showWorkMonthDashboardAll";
    }

    @GetMapping("/members/showworkmonthdashboardpopup")
    public String showWorkMonthDashboardPopup(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {
        LocalDate anchor = month != null ? month : (week != null ? week : LocalDate.now());
        addMonthNav(model, anchor, "/members/showworkmonthdashboard");

        List<MemberDto> members = List.of(memService.findById(memberId));
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        model.addAttribute("pageTitle", "월간 실 근무시간 (팝업)");
        return "members/showWorkMonthDashboardPopup";
    }

    // ────────────────────────────────────────
    //  급여 예상 팝업
    // ────────────────────────────────────────

    @GetMapping("/members/pay-estimate")
    public String showPayEstimatePopup(
            @RequestParam Long memberId,
            @RequestParam String weekStart,
            @RequestParam String weekEnd,
            Model model
    ) throws Exception {
        MemberDto memberDto = memService.findById(memberId);
        model.addAttribute("member", memberDto);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("memberJson", objectMapper.writeValueAsString(memberDto));
        return "members/payEstimatePopup";
    }

    // ────────────────────────────────────────
    //  일정 허브
    // ────────────────────────────────────────

    @GetMapping("/members/schedulehub")
    public String scheduleHub() {
        log.info("[일정허브] 일정 허브 페이지 진입");
        return "members/scheduleHub";
    }

    @GetMapping("/members/schedulemanagement")
    public String scheduleManagement(Model model) throws Exception {
        log.info("[일정관리] 일정 관리 페이지 로딩 시작");
        try {
            List<MemberDto> members = memService.findAll();
            log.info("[일정관리] 전체 근무자 목록 로딩 완료 - 총 {}명: {}",
                    members.size(),
                    members.stream().map(MemberDto::getName).toList());
            model.addAttribute("members", members);
            model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        } catch (Exception e) {
            log.error("[일정관리] 근무자 목록 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
        return "members/scheduleManagement";
    }

    @GetMapping("/members/schedulemanagementbyweek")
    public String scheduleManagementByWeek(Model model) throws Exception {
        log.info("[주간일정] 주간 일정 관리 페이지 로딩 시작");
        try {
            List<MemberDto> members = memService.findAll();
            log.info("[주간일정] 전체 근무자 목록 로딩 완료 - 총 {}명: {}",
                    members.size(),
                    members.stream().map(MemberDto::getName).toList());
            model.addAttribute("members", members);
            model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        } catch (Exception e) {
            log.error("[주간일정] 근무자 목록 조회 실패: {}", e.getMessage(), e);
            throw e;
        }
        return "members/scheduleManagementByWeek";
    }

    @GetMapping("/members/registerworkingtime")
    public String registerTodayWorkingTime(Model model) throws Exception {
        log.info("[근무등록] 오늘 근무 등록 페이지 로딩 시작");
        List<MemberDto> members = memService.findByStatus(Member.Status.WORKING);
        log.info("[근무등록] 근무중 근무자 목록 - 총 {}명: {}",
                members.size(),
                members.stream().map(MemberDto::getName).toList());
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        return "members/registerTodayWorkingTime";
    }

    // ────────────────────────────────────────
    //  근무자 CRUD
    // ────────────────────────────────────────

    @GetMapping("/members/new")
    public String registerMember() {
        return "members/new";
    }

    @GetMapping("/members/{id}/edit")
    public String editMember(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             Model model) throws Exception {
        try {
            MemberDto member = memService.findById(id);
            model.addAttribute("member", member);
            model.addAttribute("memberJson", objectMapper.writeValueAsString(member));
            addWeekNav(model, LocalDate.now(), "/members");
            return "members/edit";
        } catch (Exception ex) {
            log.error("editMember error: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("toast", "조회 중 오류가 발생하였습니다.");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/members/" + id + "/edit";
        }
    }

    @PostMapping("/members/create")
    public String createMember(@Valid @ModelAttribute MemberDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        validateMemberDto(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            log.info("createMember validation errors: {}", errorMsg);
            return "members/new";
        }

        try {
            Member saved = memService.save(dto);
            return "redirect:/members/" + saved.getId() + "/edit";
        } catch (Exception ex) {
            log.error("createMember error: {}", ex.getMessage());
            return "members/new";
        }
    }

    @PostMapping("/members/{id}/update")
    public String updateMember(@PathVariable Long id,
                               @Valid @ModelAttribute("member") MemberDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        dto.setId(id);
        validateMemberDto(dto, bindingResult);

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("toast", errorMsg);
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/members/" + id + "/edit";
        }

        try {
            memService.update(id, dto);
            redirectAttributes.addFlashAttribute("toast", "수정되었습니다.");
            redirectAttributes.addFlashAttribute("toastType", "success");
        } catch (Exception ex) {
            log.error("updateMember error: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("toast", "수정 중 오류가 발생하였습니다.");
            redirectAttributes.addFlashAttribute("toastType", "error");
        }
        return "redirect:/members/" + id + "/edit";
    }

    @GetMapping("/members/individual/print")
    public String printMember(@RequestParam Long memberId, Model model) throws Exception {
        try {
            MemberDto member = memService.findById(memberId);
            model.addAttribute("member", member);
            model.addAttribute("memberJson", objectMapper.writeValueAsString(member));
            addWeekNav(model, LocalDate.now(), "/members");
            return "members/memberInfoPrint";
        } catch (Exception ex) {
            log.error("printMember error: {}", ex.getMessage());
            return "redirect:/members/" + memberId + "/edit";
        }
    }

    @GetMapping("/members/paymenthub")
    public String paymentHub() {
        return "members/paymentHub";
    }

    // ────────────────────────────────────────
    //  공통 유효성 검사
    // ────────────────────────────────────────

    private void validateMemberDto(MemberDto dto, BindingResult bindingResult) {
        if (!StringUtils.hasText(dto.getName())) {
            bindingResult.rejectValue("name", "required", "이름은 필수입니다.");
        }
        if (dto.getHourlyWage() == null || dto.getHourlyWage() < 0) {
            bindingResult.rejectValue("hourlyWage", "min", "시간당 단가는 0 이상이어야 합니다.");
        }
        if (Boolean.TRUE.equals(dto.getHasHealthCertificate()) && dto.getHealthCertExpiry() == null) {
            bindingResult.rejectValue("healthCertExpiry", "required", "보건증 보유 시 유효기간을 입력하세요.");
        }
        if (!Boolean.TRUE.equals(dto.getHasHealthCertificate())) {
            dto.setHealthCertExpiry(null);
        }
        if (dto.getSchedule() != null) {
            dto.getSchedule().forEach(s -> {
                if (s.getStart() != null && s.getEnd() != null && !s.getEnd().isAfter(s.getStart())) {
                    bindingResult.rejectValue("schedule", "timeOrder", "종료 시간은 시작 시간보다 뒤여야 합니다.");
                }
            });
        }
    }
}
