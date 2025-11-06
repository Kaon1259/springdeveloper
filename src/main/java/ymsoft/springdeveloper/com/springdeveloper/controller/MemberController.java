package ymsoft.springdeveloper.com.springdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MemberController {

    @Autowired
    private memberService memService;

    private final ObjectMapper objectMapper; // ✅ 스프링이 모듈 등록된 ObjectMapper를 주입

    @GetMapping("/members")
    public String memberList(Model model) throws Exception {

        //List<MemberDto> members = memService.findByStatus(Member.Status.WAITING);
        List<MemberDto> members = memService.findAll();
        log.info("memberList: {}", members);

        // 2️⃣ members (뷰용 리스트)
        model.addAttribute("members", members);

        log.info(objectMapper.writeValueAsString(members));
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));

        // 4️⃣ 뷰 반환
        return "members/list";
    }

    @GetMapping("/members/realtimedashboard")
    public String realtimedashboard(Model model) throws Exception {

        List<MemberDto> members = memService.findAll();
        log.info("memberList: {}", members);

        // 2️⃣ members (뷰용 리스트)
        model.addAttribute("members", members);

        log.info(objectMapper.writeValueAsString(members));
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));
        return "members/realtimedashboard";
    }

    //금주 근무 현황
    @GetMapping("/members/thisweek")
    public String thisWeekMembers(Model model) throws Exception {
        List<MemberDto> members = memService.findAll();
        log.info("thisweek: {}", members);

        // 2️⃣ members (뷰용 리스트)
        model.addAttribute("members", members);

        log.info(objectMapper.writeValueAsString(members));
        model.addAttribute("membersJson", objectMapper.writeValueAsString(members));

        return "members/thisweek";
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
    public String editMember(@PathVariable Long id, Model model) throws Exception {
        MemberDto member = memService.findById(id);
        model.addAttribute("member", member);
        // 🔹 null-safe 값들 미리 만들어 내려주기
        String healthCertExpiryStr = (member.getHealthCertExpiry() != null)
                ? member.getHealthCertExpiry().toString() : "";
        model.addAttribute("healthCertExpiryStr", healthCertExpiryStr);

        // 스케줄도 null-safe로 내려주기
        List<MemberDto.ScheduleRow> schedules =
                (member.getSchedule() != null) ? member.getSchedule() : List.of();
        model.addAttribute("schedules", schedules);

        log.info(member.toString());

        return "members/edit";
    }


    @PostMapping("/members/create")
    public String createMember(@Valid @ModelAttribute MemberDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        log.info("memberDto: {}", dto.toString());
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
            return "members/new";
        }

        memService.save(dto);

        // 4) 리다이렉트
        return "redirect:/members"; // 목록 페이지 등 원하는 곳으로
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
            log.info("bindingResult.hasErrors(): {}", dto.toString());
            redirectAttributes.addFlashAttribute("toast", "수정도중 오류가 발생하였습니다.");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/members/" + id + "/edit";
        }

        try {
            // 실제 업데이트
            MemberDto updated = memService.update(id, dto);

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

}
