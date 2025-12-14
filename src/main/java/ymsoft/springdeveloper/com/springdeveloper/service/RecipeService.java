package ymsoft.springdeveloper.com.springdeveloper.service;

import org.springframework.transaction.annotation.Transactional;
//import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeCreateRequestDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeGroupDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeStepViewDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeViewDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
import ymsoft.springdeveloper.com.springdeveloper.entity.RecipeStep;
import ymsoft.springdeveloper.com.springdeveloper.enums.RecipeCategory;
import ymsoft.springdeveloper.com.springdeveloper.repository.RecipeRepository;

import java.util.*;
import java.util.stream.Collectors;

// Service
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public Long createRecipe(RecipeCreateRequestDto dto) {
        Recipe recipe = dto.toEntity();
        recipeRepository.save(recipe);
        return recipe.getId();
    }

    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id).orElse(null);
    }

    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public  List<Recipe> getAllRecipeWithCategory() {
        return recipeRepository.findAllByOrderByCategoryAscMenuNameAsc();
    }

    /** 리스트 화면용: 카테고리별로 그룹핑한 DTO 만들기 */
    public List<RecipeGroupDto> getGroupedRecipes() {

        List<Recipe> recipes = recipeRepository.findAllByOrderByCategoryAscMenuNameAsc();

        // 1) 엔티티 → RecipeViewDto 로 변환
        List<RecipeViewDto> viewDtos = recipes.stream()
                .map(this::toRecipeViewDto)
                .collect(Collectors.toList());

        // 2) 카테고리(RecipeCategory enum name) 기준으로 그룹핑
        Map<String, List<RecipeViewDto>> grouped =
                viewDtos.stream()
                        .collect(Collectors.groupingBy(RecipeViewDto::getCategory,
                                LinkedHashMap::new, Collectors.toList()));

        // 3) 그룹 → RecipeGroupDto 리스트로 변환 (카테고리 라벨 붙이기)
        List<RecipeGroupDto> result = new ArrayList<>();

        for (Map.Entry<String, List<RecipeViewDto>> entry : grouped.entrySet()) {
            String categoryEnumName = entry.getKey();
            List<RecipeViewDto> categoryRecipes = entry.getValue();

            String label = toCategoryLabel(categoryEnumName);

            RecipeGroupDto groupDto = RecipeGroupDto.builder()
                    .categoryLabel(label)
                    .recipes(categoryRecipes)
                    .build();

            result.add(groupDto);
        }

        return result;
    }

    @Transactional
    public void updateRecipe(Long id, RecipeCreateRequestDto dto) {

        Recipe recipe = recipeRepository.findById(id).orElse(null);

        // 기본 필드 업데이트
        recipe.setMenuName(dto.getMenuName());
        recipe.setAuthor(dto.getAuthor());
        recipe.setDescription(dto.getDescription());
        recipe.setVisible(dto.isVisible());
        recipe.setTemplate(dto.isTemplate());
        recipe.setTemperature(dto.getTemperature());
        recipe.setCupSize(dto.getCupSize());
        recipe.setCategory(dto.getCategory());

        // 기존 스텝 삭제 후 다시 추가 (가장 깔끔한 정리 방식)
        recipe.getSteps().clear();
        recipeRepository.flush();  // orphanRemoval=true → DB 실제 삭제 반영

        int order = 1;
        int stepTimeOrder = 0;
        for (String content : dto.getSteps()) {
            if (content == null || content.isBlank()) continue;

            RecipeStep step = RecipeStep.builder()
                    .recipe(recipe)
                    .stepOrder(order++)
                    .content(content.trim())
                    .stepTime(dto.getStepTimes().get(stepTimeOrder++))
                    .build();

            recipe.addStep(step);
        }
    }

    /** 단일 레시피를 리스트뷰용 DTO로 변환 */
    private RecipeViewDto toRecipeViewDto(Recipe recipe) {

        List<RecipeStepViewDto> stepDtos = Optional.ofNullable(recipe.getSteps())
                .orElseGet(Collections::emptyList)
                .stream()
                .sorted(Comparator.comparing(RecipeStep::getStepOrder))
                .map(step -> RecipeStepViewDto.builder()
                        .stepOrder(step.getStepOrder())
                        .content(step.getContent())
                        .build())
                .collect(Collectors.toList());

        return RecipeViewDto.builder()
                .id(recipe.getId())
                .menuName(recipe.getMenuName())
                .category(recipe.getCategory() != null ? recipe.getCategory().name() : null)
                .visible(recipe.isVisible())
                .template(recipe.isTemplate())
                .temperature(recipe.getTemperature() != null ? recipe.getTemperature().name() : null)
                .cupSize(recipe.getCupSize() != null ? recipe.getCupSize().name() : null)
                .steps(stepDtos)
                .build();
    }

    /** Enum 이름을 화면 표시용 한글 라벨로 변환 */
    private String toCategoryLabel(String categoryEnumName) {
        if (categoryEnumName == null) {
            return "기타";
        }

        //RecipeCategory category;
        try {
            //category = RecipeCategory.valueOf(categoryEnumName);
            return RecipeCategory
                    .valueOf(categoryEnumName)
                    .getDisplayName();

        } catch (IllegalArgumentException e) {
            // Enum에 없는 값이면 그냥 원본 출력 or "기타"
            return categoryEnumName;
        }
    }

    /**
     * visible == true 인 레시피 전체 조회 (DTO 리스트)
     */
    @Transactional(readOnly = true)
    public List<RecipeCreateRequestDto> getVisibleRecipes() {
        List<Recipe> recipes = recipeRepository.findByVisibleTrueOrderByUpdatedAtDesc();
        return RecipeCreateRequestDto.toDto(recipes);
    }

    /**
     * Mustache에서 한 행당 4개씩 카드 보여주기 편하도록
     * 4개씩 끊어서 2차원 리스트로 리턴
     *
     * ex) [ [레시피1,2,3,4], [레시피5,6,7,8], ... ]
     */
    @Transactional(readOnly = true)
    public List<List<RecipeCreateRequestDto>> getVisibleRecipesChunked(int chunkSize) {
        List<RecipeCreateRequestDto> all = getVisibleRecipes();
        List<List<RecipeCreateRequestDto>> result = new ArrayList<>();

        if (all == null || all.isEmpty()) {
            return result;
        }

        for (int i = 0; i < all.size(); i += chunkSize) {
            int toIndex = Math.min(i + chunkSize, all.size());
            result.add(all.subList(i, toIndex));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<List<RecipeCreateRequestDto>> getVisibleRecipesByRowOf4() {
        return getVisibleRecipesChunked(4);
    }

    public List<RecipeCreateRequestDto> getTemplateRecipes() {
        List<Recipe> recipes = recipeRepository.findAllByTemplateTrueOrderByCategoryAscMenuNameAsc();
        return RecipeCreateRequestDto.toDto(recipes);
    }

    @Transactional
    public void updateVisible(Long recipeId, boolean visible) {
        int updated = recipeRepository.updateVisible(recipeId, visible);
        if (updated == 0) {
            throw new IllegalArgumentException("레시피를 찾을 수 없습니다. id=" + recipeId);
        }
    }

    @Transactional
    public void updateTemplate(Long recipeId, boolean template) {
        int updated = recipeRepository.updateTemplate(recipeId, template);
        if (updated == 0) {
            throw new IllegalArgumentException("레시피를 찾을 수 없습니다. id=" + recipeId);
        }
    }

    @Transactional
    public void deleteRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found. id=" + recipeId));

        recipeRepository.delete(recipe);
    }
}
