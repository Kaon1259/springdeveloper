package ymsoft.springdeveloper.com.springdeveloper.cafe;

import jakarta.servlet.http.HttpServletRequest;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/cafe")
public class CafeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping("")
    public String cafe(Model model) {

        // visible == true 레시피를 4개씩 끊어서 가져오기
        List<RecipeCreateRequestDto> recipes = recipeService.getVisibleRecipes();

        model.addAttribute("recipeRows", recipes);

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

    @GetMapping("/recipes/onlyread")
    public String onlyReadList(Model model) {
        List<RecipeGroupDto> recipeGroups = recipeService.getGroupedRecipes();

        log.info("recipeGroups: {}", recipeGroups);

        long totalCount = recipeGroups.stream()
                .mapToLong(g -> g.getRecipes().size())
                .sum();

        log.info("totalCount: {}", totalCount);
        model.addAttribute("recipeGroups", recipeGroups);
        model.addAttribute("totalCount", totalCount);

        return "cafe/recipeListForOnlyRead";
    }

    @GetMapping("/recipes/onlyread2")
    public String onlyReadList2(Model model) {
        List<RecipeGroupDto> recipeGroups = recipeService.getGroupedRecipes();

        log.info("recipeGroups: {}", recipeGroups);

        long totalCount = recipeGroups.stream()
                .mapToLong(g -> g.getRecipes().size())
                .sum();

        log.info("totalCount: {}", totalCount);
        model.addAttribute("recipeGroups", recipeGroups);
        model.addAttribute("totalCount", totalCount);

        return "cafe/recipeListForOnlyRead2";
    }


    @GetMapping("/recipe/new")
    public String showCreateForm(Model model) {
        List<RecipeCreateRequestDto>  templateRecipesDto = recipeService.getTemplateRecipes();
        model.addAttribute("templateRecipes", templateRecipesDto);

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
    public String update(@PathVariable Long id, @ModelAttribute RecipeCreateRequestDto dto, HttpServletRequest request) {
        log.info("useBlender param values={}", Arrays.toString(request.getParameterValues("useBlender")));
        log.info("update recipe: {}", dto);

        recipeService.updateRecipe(id, dto);

        log.info("update recipe id: {}", id);

        return "redirect:/cafe/recipe/" + id + "/edit";
    }
}
