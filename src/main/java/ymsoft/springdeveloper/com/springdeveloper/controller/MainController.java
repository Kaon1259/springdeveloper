package ymsoft.springdeveloper.com.springdeveloper.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ymsoft.springdeveloper.com.springdeveloper.dto.UserDto;
import ymsoft.springdeveloper.com.springdeveloper.service.UserService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginFrame() {
        return "login/login";
    }

    @GetMapping("/signup")
    public String signupFrame() {
        return "login/signup";
    }

    @PostMapping("/signup")
    public String join(@ModelAttribute @Valid UserDto request,
                       BindingResult bindingResult) {

        log.info("signup request received");

        boolean isAgreed = Boolean.TRUE.equals(request.getAgreedTerms());
        if (!isAgreed) {
            bindingResult.rejectValue("agreedTerms", "terms.required",
                    "이용약관에 동의해야 회원가입이 가능합니다.");
        }

        if (!request.isPasswordMatched()) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch",
                    "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            log.info("signup errors: {}", bindingResult.getAllErrors());
            return "login/signup";
        }

        try {
            Long id = userService.signup(request);
            if (id != null) {
                return "redirect:/login";
            }
            return "redirect:/signup";
        } catch (Exception e) {
            log.error("signup error: {}", e.getMessage());
            return "redirect:/signup";
        }
    }
}
