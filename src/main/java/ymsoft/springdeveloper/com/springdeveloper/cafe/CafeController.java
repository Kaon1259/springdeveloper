package ymsoft.springdeveloper.com.springdeveloper.cafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ymsoft.springdeveloper.com.springdeveloper.dto.MenuDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeCreateRequestDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeGroupDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
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

    @GetMapping("/recipes")
    public String list(Model model) {
        List<RecipeGroupDto> recipeGroups = recipeService.getGroupedRecipes();

        log.info("recipeGroups: {}", recipeGroups);

        long totalCount = recipeGroups.stream()
                .mapToLong(g -> g.getRecipes().size())
                .sum();

        log.info("totalCount: {}", totalCount);
        model.addAttribute("recipeGroups", recipeGroups);
        model.addAttribute("totalCount", totalCount);

        return "cafe/recipeList";
    }


    @GetMapping("/recipe/new")
    public String showCreateForm() {
        return "cafe/recipeNewForm";
    }


    @GetMapping("/recipe/{id}/edit")
    public String edit(@PathVariable Long id,
                       Model model) {
        log.info("edit : id: " + id);
        Recipe recipe = recipeService.getRecipeById(id);

        // 2) 엔티티 → DTO 변환 (폼에 뿌릴 값 세팅)
        RecipeCreateRequestDto dto = RecipeCreateRequestDto.toForm(recipe);

        log.info("dto: " + dto);

        // 3) 모델에 담아서 뷰로 전달
        model.addAttribute("recipeId", id);   // hidden 필드용 등
        model.addAttribute("recipe", dto);
        return "cafe/recipeEditForm";
    }

    @PostMapping("/recipe/save")
    public String save(@ModelAttribute RecipeCreateRequestDto dto) {

        log.info("save recipe: {}", dto);
        Long id = recipeService.createRecipe(dto);

        log.info("save recipe id: {}", id);

        return "redirect:/cafe/recipe/" + id + "/edit";
    }

    @PostMapping("/recipe/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute RecipeCreateRequestDto dto) {

        log.info("update recipe: {}", dto);
        recipeService.updateRecipe(id, dto);

        log.info("update recipe id: {}", id);

        return "redirect:/cafe/recipe/" + id + "/edit";
    }
}
