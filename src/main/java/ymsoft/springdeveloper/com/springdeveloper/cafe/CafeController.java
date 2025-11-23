package ymsoft.springdeveloper.com.springdeveloper.cafe;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ymsoft.springdeveloper.com.springdeveloper.dto.MenuDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeCreateRequestDto;
import ymsoft.springdeveloper.com.springdeveloper.service.RecipeService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/cafe")
public class CafeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping("")
    public String cafe(Model model) {

        List<MenuDto> menuList = List.of(
                MenuDto.builder().id(1L).name("아메리카노").category("커피").hotOrIced("HOT/ICED").isSignature(true).build(),
                MenuDto.builder().id(2L).name("카페라떼").category("커피").hotOrIced("HOT/ICED").isSignature(false).build(),
                MenuDto.builder().id(3L).name("바닐라라떼").category("커피").hotOrIced("ICED").isSignature(true).build(),
                MenuDto.builder().id(4L).name("딸기스무디").category("스무디").hotOrIced("ICED").isSignature(false).build(),
                MenuDto.builder().id(5L).name("카페라떼").category("커피").hotOrIced("ICED").isSignature(true).build(),
                MenuDto.builder().id(6L).name("캐라멜마키아또").category("커피").hotOrIced("ICED").isSignature(false).build()
        );

        model.addAttribute("menuList", menuList);
        return "cafe/cafeRecipeSelector";

    }

    @GetMapping("/edit")
    public String cafeEditor() {

        return "cafe/recipeEditor";
    }

    @PostMapping("/recipe/save")
    public String save(@ModelAttribute RecipeCreateRequestDto dto) {

        log.info("save recipe: {}", dto);
       // Long id = recipeService.createRecipe(dto);
        // return "redirect:/recipes/" + id;

        return "";
    }

}
