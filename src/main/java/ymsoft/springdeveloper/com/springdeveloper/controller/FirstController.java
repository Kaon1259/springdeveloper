package ymsoft.springdeveloper.com.springdeveloper.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FirstController {
    @GetMapping("/greetings")
    public String niceToMeetYou(Model model) {
        model.addAttribute("nickname", "Hello World");
        return "greetings";
    }

    @GetMapping("/goodbye")
    public String goodbye(Model model) {
        model.addAttribute("nickname", "hong Park");
        return "goodbye";
    }
}
