package ymsoft.springdeveloper.com.springdeveloper.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ymsoft.springdeveloper.com.springdeveloper.dto.UserDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.UserLoginRequest;
import ymsoft.springdeveloper.com.springdeveloper.entity.Users;
import ymsoft.springdeveloper.com.springdeveloper.service.UserService;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private UserService userService;

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

        log.info("signup");
        return "login/signup";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") @Valid UserLoginRequest request,
                        BindingResult bindingResult) {

        log.info("login" + request.toString());

        if (bindingResult.hasErrors()) {
            log.info("login error " + bindingResult.getAllErrors());
            return "login/login";
        }

        try{
            Users user = userService.signin(request.getEmail(), request.getPassword());

            if(user != null) {
                return "redirect:/members";
            }else {
                return "redirect:/login";
            }
        } catch (Exception e) {
            log.info("login error " + e);
            bindingResult.reject("loginError", "로그인 처리 중 오류가 발생했습니다.");
            return "login/login";
        }
    }

    @PostMapping("/signup")
    public String join(@ModelAttribute @Valid UserDto request,
                       BindingResult bindingResult) {

        log.info("signup = " + request.toString());
        // 체크박스 값 매핑
        request.applyTermsCheckbox(request.getAgreedTerms() != null ? "Y" : null);
        boolean isAgreed = request.getAgreedTerms() != null ? true : false;

        request.setAgreedTerms(isAgreed);
        log.info("signuu dto = " + request.toString());

        if (!isAgreed) {
            bindingResult.rejectValue("agreedTerms", "terms.required",
                    "이용약관에 동의해야 회원가입이 가능합니다.");
        }

        // 비번 확인 체크
        if (!request.isPasswordMatched()) {
            bindingResult.rejectValue("passwordConfirm", "password.mismatch",
                    "비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            log.info("errors = " + bindingResult.getAllErrors());
            return "memberJoin";  // 다시 폼으로
        }

        try {
            Long id = userService.signup(request);

            if (id != null) {
                return "redirect:/login";
            }
            return "redirect:/signup";

        } catch (Exception e) {
            log.error(e.getMessage());
            return "signup";
        }
    }
}
