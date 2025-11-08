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
import ymsoft.springdeveloper.com.springdeveloper.service.memberService;

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
}
