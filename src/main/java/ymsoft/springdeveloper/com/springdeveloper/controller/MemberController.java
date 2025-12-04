package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    @Autowired
    private memberService memService;

    @Autowired
    private  WorkScheduleService workScheduleService;

    private final ObjectMapper objectMapper; // ✅ 스프링이 모듈 등록된 ObjectMapper를 주입

    @GetMapping("/members")
    public String memberList(
            @RequestParam(name = "week", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {

        // 1) 기준 주(월요일~일요일) 계산
        LocalDate base = (week != null ? week : LocalDate.now());
        LocalDate startOfWeek = base.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek   = startOfWeek.plusDays(6);

        // 2) 라벨/URL
        DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter md  = DateTimeFormatter.ofPattern("MM.dd");

        String weekRangeLabel = String.format("%s ~ %s",
                startOfWeek.format(ymd), endOfWeek.format(md));

        // 예: /members?week=2025-11-03
        String weekPrevUrl = "/members?week=" + startOfWeek.minusWeeks(1);
        String weekNextUrl = "/members?week=" + startOfWeek.plusWeeks(1);

        // 3) 멤버 목록
        List<MemberDto> members = memService.findAll();
        model.addAttribute("members", members);
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));

        // 4) 주간 네비게이션 바인딩
        model.addAttribute("weekRangeLabel", weekRangeLabel);
        model.addAttribute("weekPrevUrl", weekPrevUrl);
        model.addAttribute("weekNextUrl", weekNextUrl);

        return "members/list";
    }

    @GetMapping("/members/pay-estimate")
    public String showPayEstimatePopup(
            @RequestParam Long memberId,
            @RequestParam String weekStart,
            @RequestParam String weekEnd,
            Model model
    ) throws Exception {
        MemberDto memberDto = memService.findById(memberId);

        log.info(memberDto.toString());

        model.addAttribute("member", memberDto);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);

        String memberJson = objectMapper.writeValueAsString(memberDto);
        model.addAttribute("memberJson", memberJson);

        return "members/payEstimatePopup";
    }

    @GetMapping("/members/realtimedashboard")
    public String realtimedashboard(Model model) throws Exception {

        log.info("realtimedashboard");
        // 📆 오늘 날짜
        LocalDate today = LocalDate.now();

        // 1) 기존처럼 전체 멤버 조회 (MemberDto 사용)
        //List<MemberDto> members = memService.findAll();
        List<MemberDto> members = memService.findByStatus(Member.Status.WORKING);

        // 2) 오늘 실제 근무 일정 조회 (WorkSchedule)
        //   -> WorkScheduleService / Repository에서 가져오도록 가정
        List<WorkSchedule> todaySchedules = workScheduleService.findByWorkDateWithMember(today);

        // 3) memberId 기준으로 그룹핑
        Map<Long, List<WorkSchedule>> wsByMember = todaySchedules.stream()
                .collect(Collectors.groupingBy(ws -> ws.getMember().getId()));

        // 4) 뷰에 내려줄 DTO로 변환 (JS가 쓰기 쉬운 형태)
        String dayKey = toDayKey(today); // "MON" / "TUE" ... "SUN"

        List<MemberRealtimeDto> viewMembers = members.stream()
                .map(m -> MemberRealtimeDto.from(m, wsByMember.getOrDefault(m.getId(), List.of()), dayKey))
                .collect(Collectors.toList());

        model.addAttribute("members", viewMembers);

        String json = objectMapper.writeValueAsString(viewMembers);
        log.info("memberRealtime: {}", json);
        model.addAttribute("membersJson", json);

        return "members/realtimedashboard";
    }

    /** LocalDate -> "MON" / "TUE" ... "SUN" */
    private String toDayKey(LocalDate date) {
        // java.time.DayOfWeek: MONDAY, TUESDAY ...
        String name = date.getDayOfWeek().name(); // "FRIDAY"
        return name.substring(0, 3);             // "FRI"
    }


    //일정
    @GetMapping("/members/showworktimehub")
    public String showWorkTimeHub() throws Exception {
        return "members/showWorkTimeHub";
    }

    @GetMapping("/members/showworktimedashboard")
    public String showWorkTimeDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week, // 주 기준일(월요일) 선택 파라미터
            Model model
    ) throws Exception {

        // 1) 기준 주 계산 (월요일 시작)
        LocalDate base = (week != null) ? week : LocalDate.now();
        LocalDate startOfWeek = base.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek   = startOfWeek.plusDays(6);

        // 2) 주간 라벨/이동 URL (⚠ 템플릿은 평탄 키 사용: weekRangeLabel / weekPrevUrl / weekNextUrl)
        model.addAttribute("weekRangeLabel",
                String.format("%s ~ %s",
                        startOfWeek.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        endOfWeek.format(DateTimeFormatter.ofPattern("MM.dd"))));
        model.addAttribute("weekPrevUrl", "/members/showworktimedashboard?week=" + startOfWeek.minusWeeks(1));
        model.addAttribute("weekNextUrl", "/members/showworktimedashboard?week=" + startOfWeek.plusWeeks(1));

        // 3) 멤버 목록 (좌측 리스트 & 우측 주간표 데이터 소스)
        List<MemberDto> members = memService.findAll();
        log.info("showworktimedashboard members: {}", members);

        // 필요한 경우 서버에서 정렬/필터를 미리 적용해도 됩니다.
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("showWorktimeDashboard membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // (선택) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");

        return "members/showWorktimeDashboard"; // Mustache 파일 경로/이름
    }

    @GetMapping("/members/showworktimedashboardall")
    public String showWorkTimeDashboardAll(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week, // 주 기준일(월요일) 선택 파라미터
            Model model
    ) throws Exception {

        // 1) 기준 주 계산 (월요일 시작)
        LocalDate base = (week != null) ? week : LocalDate.now();
        LocalDate startOfWeek = base.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek   = startOfWeek.plusDays(6);

        // 2) 주간 라벨/이동 URL (⚠ 템플릿은 평탄 키 사용: weekRangeLabel / weekPrevUrl / weekNextUrl)
        model.addAttribute("weekRangeLabel",
                String.format("%s ~ %s",
                        startOfWeek.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        endOfWeek.format(DateTimeFormatter.ofPattern("MM.dd"))));
        model.addAttribute("weekPrevUrl", "/members/showworktimedashboard?week=" + startOfWeek.minusWeeks(1));
        model.addAttribute("weekNextUrl", "/members/showworktimedashboard?week=" + startOfWeek.plusWeeks(1));

        // 3) 멤버 목록 (좌측 리스트 & 우측 주간표 데이터 소스)
        List<MemberDto> members = memService.findAll();
        log.info("showworktimedashboard members: {}", members);

        // 필요한 경우 서버에서 정렬/필터를 미리 적용해도 됩니다.
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("showWorktimeDashboard membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // (선택) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");

        return "members/showWorktimeDashboardAll"; // Mustache 파일 경로/이름
    }

    @GetMapping("/members/showworktimedashboardpopup")
    public String showWorkTimeDashboardPopup(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) throws Exception {

        // 1) 기준 주 계산 (월요일 시작)
        LocalDate base = (startDate != null) ? startDate : LocalDate.now();
        LocalDate startOfWeek = base.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek   = startOfWeek.plusDays(6);

        model.addAttribute("weekRangeLabel",
                String.format("%s ~ %s",
                        startOfWeek.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
                        endOfWeek.format(DateTimeFormatter.ofPattern("MM.dd"))));

        model.addAttribute("weekPrevUrl", "/members/showworktimedashboardpopup?memberId=" + memberId
                + "&startDate=" + startOfWeek.minusWeeks(1)
                + "&endDate=" + startOfWeek.minusWeeks(1).plusDays(6));

        model.addAttribute("weekNextUrl", "/members/showworktimedashboardpopup?memberId=" + memberId
                + "&startDate=" + startOfWeek.plusWeeks(1)
                + "&endDate=" + startOfWeek.plusWeeks(1).plusDays(6));

        // ★ findById → 리스트로 감싸기
        MemberDto member = memService.findById(memberId);
        List<MemberDto> members = List.of(member);

        log.info("showworktimedashboardpopup members: {}", members);

        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members); // ★ 배열 JSON
        log.info("showWorktimeDashboardPopup membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드(팝업)");

        return "members/showWorktimeDashboardPopup";
    }


    @GetMapping("/members/showworkmonthdashboard")
    public String showWorkMonthDashboard(
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

        return "members/showWorkMonthDashboard"; // 머스태시 템플릿
    }

    @GetMapping("/members/showworkmonthdashboardpopup")
    public String showWorkMonthDashboardPopup(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
            Model model
    ) throws Exception {

        log.info("showworkmonthdashboardpopup memberId: {}", memberId);
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

        // ★ findById → 리스트로 감싸기
        MemberDto member = memService.findById(memberId);
        List<MemberDto> members = List.of(member);

        log.info("showworkmonthdashboard members: {}", members);
        model.addAttribute("members", members);

        String membersJson = objectMapper.writeValueAsString(members);
        log.info("showworkmonthdashboard membersJson: {}", membersJson);
        model.addAttribute("membersJson", membersJson);

        // 5) 페이지 타이틀
        model.addAttribute("pageTitle", "월/주 실 근무시간 대시보드");

        return "members/showWorkMonthDashboardPopup"; // 머스태시 템플릿
    }

    //일정
    @GetMapping("/members/schedulehub")
    public String scheduleHub(Model model) throws Exception {
        return "members/scheduleHub";
    }

    //금주 근무 현황
    @GetMapping("/members/schedulemanagement")
    public String thisWeekMembers(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        log.info("schedulemanagement: {}", members);

        // 2️⃣ members (뷰용 리스트)
        model.addAttribute("members", members);

        log.info(objectMapper.writeValueAsString(members));
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));

        return "members/scheduleManagement";
    }

    //금주 근무 현황
    @GetMapping("/members/schedulemanagementbyweek")
    public String thisWeekMembersByWeek(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        log.info("schedulemanagementbyweek: {}", members);

        // 2️⃣ members (뷰용 리스트)
        model.addAttribute("members", members);

        log.info(objectMapper.writeValueAsString(members));
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));

        return "members/scheduleManagementByWeek";
    }

    //금일 실 근무 시간 등록
    @GetMapping("/members/registerworkingtime")
    public String registerTodayWorkingTime(Model model) throws Exception {
        List<MemberDto> members = memService.findByStatus(Member.Status.WORKING);
        log.info("registerworkingtime: {}", members);

        // 2️⃣ members (뷰용 리스트)
        model.addAttribute("members", members);

        log.info(objectMapper.writeValueAsString(members));
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));

        return "members/registerTodayWorkingTime";
    }

    //신규 등록
    @GetMapping("/members/new")
    public String registerMember() {
        return "members/new";
    }

    //인사정보 보기
    @GetMapping("/members/{id}/edit")
    public String editMember(@PathVariable Long id,
                             RedirectAttributes redirectAttributes,
                             Model model) throws Exception {

        try {
            log.info("editMember: {}", id);
            MemberDto member = memService.findById(id);
            member.getHealthCertExpiryStr();

            model.addAttribute("member", member);

            // 1) 기준 주(월요일~일요일) 계산
            LocalDate base =  LocalDate.now();
            LocalDate startOfWeek = base.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek   = startOfWeek.plusDays(6);

            // 2) 라벨/URL
            DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter md  = DateTimeFormatter.ofPattern("MM.dd");

            String weekRangeLabel = String.format("%s ~ %s",
                    startOfWeek.format(ymd), endOfWeek.format(md));

            // 예: /members?week=2025-11-03
            String weekPrevUrl = "/members?week=" + startOfWeek.minusWeeks(1);
            String weekNextUrl = "/members?week=" + startOfWeek.plusWeeks(1);


            // 4) 주간 네비게이션 바인딩
            model.addAttribute("weekRangeLabel", weekRangeLabel);
            model.addAttribute("weekPrevUrl", weekPrevUrl);
            model.addAttribute("weekNextUrl", weekNextUrl);

            String memberJson = objectMapper.writeValueAsString(member);
            model.addAttribute("memberJson", memberJson);

            log.info(member.toString());

            return "members/edit";

        } catch (Exception ex) {
            log.info("Exception : /members/{id}/edit: {}", ex.toString());
            // 예외 처리: 에러 메시지와 함께 편집 화면 복귀
            redirectAttributes.addFlashAttribute("toast", "수정도중 오류가 발생하였습니다.");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/members/" + id + "/edit";
        }

    }


    @PostMapping("/members/create")
    public String createMember(@Valid @ModelAttribute MemberDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        log.info("createMember:memberDto: {}", dto.toString());

        // 1) 서버측 기본 검증
        if (!StringUtils.hasText(dto.getName())) {
            bindingResult.rejectValue("name", "required", "이름은 필수입니다.");
        }
        if (dto.getHourlyWage() == null || dto.getHourlyWage() < 0) {
            bindingResult.rejectValue("hourlyWage", "min", "시간당 단가는 0 이상이어야 합니다.");
        }
        // 보건증 보유 여부에 따른 유효기간 처리
        if (Boolean.TRUE.equals(dto.getHasHealthCertificate())) {
             //보건증 보유인데 유효기간이 없는 경우는 허용(선택)하되, 필요 시 아래 주석 해제
             if (dto.getHealthCertExpiry() == null) {
                 log.info("healthCertExpiry is empty");
                 bindingResult.rejectValue("healthCertExpiry", "required", "보건증 보유 시 유효기간을 입력하세요.");
             }
        } else {
            // 미보유면 만료일 무시
            dto.setHealthCertExpiry(null);
        }

        // 스케줄 유효성(선택)
        if (dto.getSchedule() != null) {
            dto.getSchedule().forEach(s -> {
                if (s.getStart() != null && s.getEnd() != null && !s.getEnd().isAfter(s.getStart())) {
                    bindingResult.rejectValue("schedule", "timeOrder", "종료 시간은 시작 시간보다 뒤여야 합니다.");
                }
            });
        }

        if (bindingResult.hasErrors()) {
            // 에러 시, 다시 폼으로(템플릿 경로는 프로젝트에 맞게)
            String errorMessage = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));
            log.info("bindingResult.hasErrors(): " + errorMessage);
            return "members/new";
        }

        try {
            Member saved = memService.save(dto);
            // 4) 리다이렉트
            log.info("createMember: saved: {}", saved.toString());
            return "redirect:/members/" + saved.getId() + "/edit"; // 목록 페이지 등 원하는 곳으로

        }catch (Exception ex) {
            log.info("Exception : /members/{id}/edit: {}", ex.toString());
            return "members/new";
        }
    }


    @PostMapping("/members/{id}/update")
    public String updateMember(@PathVariable Long id,
                               @Valid @ModelAttribute("member") MemberDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        log.info("/members/{id}/update: {}", dto.toString());

        // path의 id를 DTO에 반영(신뢰원 통일)
        dto.setId(id);

        // --- 서버측 기본 검증 ---
        if (!org.springframework.util.StringUtils.hasText(dto.getName())) {
            log.info("name is empty");
            bindingResult.rejectValue("name", "required", "이름은 필수입니다.");
        }
        if (dto.getHourlyWage() == null || dto.getHourlyWage() < 0) {
            log.info("hourlyWage is empty");
            bindingResult.rejectValue("hourlyWage", "min", "시간당 단가는 0 이상이어야 합니다.");
        }


        log.info("hasHealthCertificate: {}", dto.getHasHealthCertificate());
        // 보건증 보유/만료일 처리
        if (Boolean.TRUE.equals(dto.getHasHealthCertificate())) {
            log.info("hasHealthCertificate is empty");
            // 필요 시 만료일 필수화
             if (dto.getHealthCertExpiry() == null) {
                 log.info("healthCertExpiry is empty");
                 bindingResult.rejectValue("healthCertExpiry", "required", "보건증 보유 시 유효기간을 입력하세요.");
             }
        } else {
            log.info("hasHealthCertificate is empty");
            dto.setHealthCertExpiry(null); // 미보유면 만료일 무시
        }

        // 스케줄 시간순 검증
        if (dto.getSchedule() != null) {
            dto.getSchedule().forEach(s -> {
                if (s.getStart() != null && s.getEnd() != null && !s.getEnd().isAfter(s.getStart())) {
                    log.info("종료 시간은 시작 시간보다 뒤여야 합니다.");
                    bindingResult.rejectValue("schedule", "timeOrder", "종료 시간은 시작 시간보다 뒤여야 합니다.");
                }
            });
        }

        // --- 에러 시: edit.mustache가 기대하는 모델 값 복구 ---
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            log.info("Errors: {}", errorMessage);

            redirectAttributes.addFlashAttribute("toast", errorMessage);
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/members/" + id + "/edit";
        }

        try {
            // 실제 업데이트
            MemberDto updated = memService.update(id, dto);

            log.info("updated: {}", updated.toString());
            // 성공 알림 후 상세 편집 화면으로 리다이렉트
            redirectAttributes.addFlashAttribute("toast", "수정되었습니다.");
            redirectAttributes.addFlashAttribute("toastType", "success");
            return "redirect:/members/" + id + "/edit";

        } catch (Exception ex) {
            log.info("Exception : /members/{id}/update: {}", ex.toString());
            // 예외 처리: 에러 메시지와 함께 편집 화면 복귀
            redirectAttributes.addFlashAttribute("toast", "수정도중 오류가 발생하였습니다.");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/members/" + id + "/edit";
        }
    }


    //인사정보 보기
    @GetMapping("/members/individual/print")
    public String printMember(@RequestParam Long memberId,
                             Model model) throws Exception {

        try {
            log.info("printMember: {}", memberId);
            MemberDto member = memService.findById(memberId);
            member.getHealthCertExpiryStr();

            model.addAttribute("member", member);

            // 1) 기준 주(월요일~일요일) 계산
            LocalDate base =  LocalDate.now();
            LocalDate startOfWeek = base.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek   = startOfWeek.plusDays(6);

            // 2) 라벨/URL
            DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            DateTimeFormatter md  = DateTimeFormatter.ofPattern("MM.dd");

            String weekRangeLabel = String.format("%s ~ %s",
                    startOfWeek.format(ymd), endOfWeek.format(md));

            // 예: /members?week=2025-11-03
            String weekPrevUrl = "/members?week=" + startOfWeek.minusWeeks(1);
            String weekNextUrl = "/members?week=" + startOfWeek.plusWeeks(1);


            // 4) 주간 네비게이션 바인딩
            model.addAttribute("weekRangeLabel", weekRangeLabel);
            model.addAttribute("weekPrevUrl", weekPrevUrl);
            model.addAttribute("weekNextUrl", weekNextUrl);

            String memberJson = objectMapper.writeValueAsString(member);
            model.addAttribute("memberJson", memberJson);

            log.info(member.toString());

            return "members/memberInfoPrint";

        } catch (Exception ex) {
            log.info("Exception : printMember: {}", ex.toString());
            return "redirect:/members/" + memberId + "/edit";
        }
    }
}
