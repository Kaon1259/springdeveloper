package ymsoft.springdeveloper.com.springdeveloper.cafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/mega")
public class MegaController {

    @GetMapping("")
    public String mega() {
        return "mega/hub";
    }
}
