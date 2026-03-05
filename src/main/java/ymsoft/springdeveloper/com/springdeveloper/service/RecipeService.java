package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.CategoryReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.FavoriteReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeCreateRequestDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeGroupDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeStepViewDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeViewDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
import ymsoft.springdeveloper.com.springdeveloper.entity.RecipeStep;
import ymsoft.springdeveloper.com.springdeveloper.enums.RecipeCategory;
import ymsoft.springdeveloper.com.springdeveloper.mapper.RecipeMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeMapper recipeMapper;

    @Transactional
    public Long createRecipe(RecipeCreateRequestDto dto) {
        Recipe recipe = dto.toEntity();
        recipeMapper.insert(recipe); // useGeneratedKeys → recipe.id 채워짐

        List<RecipeStep> steps = recipe.getSteps();
        if (steps != null) {
            for (RecipeStep step : steps) {
                step.setRecipeId(recipe.getId());
                step.setRecipe(null); // 순환참조 방지
                recipeMapper.insertStep(step);
            }
        }
        return recipe.getId();
    }

    public Recipe getRecipeById(Long id) {
        return recipeMapper.findById(id).orElse(null);
    }

    public List<Recipe> getAllRecipes() {
        return recipeMapper.findAllOrderByCategoryMenuName();
    }

    public List<Recipe> getAllRecipeWithCategory() {
        return recipeMapper.findAllOrderByCategoryMenuName();
    }

    public List<RecipeGroupDto> getGroupedRecipes() {
        List<Recipe> recipes = recipeMapper.findAllOrderByCategoryMenuName();

        List<RecipeViewDto> viewDtos = recipes.stream()
                .map(this::toRecipeViewDto)
                .collect(Collectors.toList());

        Map<String, List<RecipeViewDto>> grouped =
                viewDtos.stream()
                        .collect(Collectors.groupingBy(
                                dto -> dto.getCategory() != null ? dto.getCategory() : "기타",
                                LinkedHashMap::new, Collectors.toList()));

        // 카테고리 순서 DB에서 조회
        List<Map<String, Object>> catOrderRows = recipeMapper.findAllCategoryOrders();
        Map<String, Integer> catOrderMap = new HashMap<>();
        for (Map<String, Object> row : catOrderRows) {
            String catName = (String) row.get("category_name");
            Number sortOrd = (Number) row.get("sort_order");
            if (catName != null) {
                catOrderMap.put(catName, sortOrd != null ? sortOrd.intValue() : 0);
            }
        }

        List<RecipeGroupDto> result = new ArrayList<>();
        for (Map.Entry<String, List<RecipeViewDto>> entry : grouped.entrySet()) {
            String categoryEnumName = entry.getKey();
            List<RecipeViewDto> categoryRecipes = entry.getValue();
            String label = toCategoryLabel(categoryEnumName);
            result.add(RecipeGroupDto.builder()
                    .categoryKey(categoryEnumName)
                    .categoryLabel(label)
                    .recipes(categoryRecipes)
                    .build());
        }

        // DB 기반 카테고리 순서로 정렬 (DB에 없는 카테고리는 뒤로)
        result.sort(Comparator.comparingInt(g ->
                catOrderMap.getOrDefault(g.getCategoryKey(), Integer.MAX_VALUE)));

        return result;
    }

    @Transactional
    public void updateRecipe(Long id, RecipeCreateRequestDto dto) {
        Recipe recipe = recipeMapper.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("레시피를 찾을 수 없습니다. id=" + id));

        recipe.setMenuName(dto.getMenuName());
        recipe.setAuthor(dto.getAuthor());
        recipe.setDescription(dto.getDescription());
        recipe.setImgname(dto.getImgname());
        recipe.setVisible(dto.isVisible());
        recipe.setTemplate(dto.isTemplate());
        recipe.setTemperature(dto.getTemperature());
        recipe.setUseBlender(dto.isUseBlender());
        recipe.setCupSize(dto.getCupSize());
        recipe.setCategory(dto.getCategory());
        recipeMapper.update(recipe);

        // 기존 스텝 삭제 후 재삽입
        recipeMapper.deleteStepsByRecipeId(id);

        int order = 1;
        List<String> steps = dto.getSteps();
        List<Integer> times = dto.getStepTimes();
        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                String content = steps.get(i);
                if (content == null || content.isBlank()) continue;
                Integer stepTime = (times != null && i < times.size()) ? times.get(i) : null;
                RecipeStep step = RecipeStep.builder()
                        .recipeId(id)
                        .stepOrder(order++)
                        .content(content.trim())
                        .stepTime(stepTime)
                        .build();
                recipeMapper.insertStep(step);
            }
        }
    }

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
                .category(recipe.getCategory() != null ? recipe.getCategory().name() : "기타")
                .visible(recipe.isVisible())
                .template(recipe.isTemplate())
                .useBlender(recipe.isUseBlender())
                .imgname(recipe.getImgname())
                .temperature(recipe.getTemperature() != null ? recipe.getTemperature().name() : null)
                .cupSize(recipe.getCupSize() != null ? recipe.getCupSize().name() : null)
                .steps(stepDtos)
                .build();
    }

    private String toCategoryLabel(String categoryEnumName) {
        if (categoryEnumName == null) return "기타";
        try {
            return RecipeCategory.valueOf(categoryEnumName).getDisplayName();
        } catch (IllegalArgumentException e) {
            return categoryEnumName;
        }
    }

    @Transactional(readOnly = true)
    public List<RecipeCreateRequestDto> getVisibleRecipes() {
        List<Recipe> recipes = recipeMapper.findByVisibleTrueOrderByFavoritePosition();
        return RecipeCreateRequestDto.toDto(recipes);
    }

    @Transactional
    public void reorderFavorites(FavoriteReorderDto dto) {
        if (dto.getOrders() == null || dto.getOrders().isEmpty()) return;
        recipeMapper.upsertFavoriteOrders(dto.getOrders());
    }

    @Transactional(readOnly = true)
    public List<List<RecipeCreateRequestDto>> getVisibleRecipesChunked(int chunkSize) {
        List<RecipeCreateRequestDto> all = getVisibleRecipes();
        List<List<RecipeCreateRequestDto>> result = new ArrayList<>();
        if (all == null || all.isEmpty()) return result;
        for (int i = 0; i < all.size(); i += chunkSize) {
            result.add(all.subList(i, Math.min(i + chunkSize, all.size())));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<List<RecipeCreateRequestDto>> getVisibleRecipesByRowOf4() {
        return getVisibleRecipesChunked(4);
    }

    public List<RecipeCreateRequestDto> getTemplateRecipes() {
        return RecipeCreateRequestDto.toDto(recipeMapper.findAllByTemplateTrueOrderByCategoryMenuName());
    }

    @Transactional
    public void updateVisible(Long recipeId, boolean visible) {
        int updated = recipeMapper.updateVisible(recipeId, visible);
        if (updated == 0) throw new IllegalArgumentException("레시피를 찾을 수 없습니다. id=" + recipeId);
    }

    @Transactional
    public void updateTemplate(Long recipeId, boolean template) {
        int updated = recipeMapper.updateTemplate(recipeId, template);
        if (updated == 0) throw new IllegalArgumentException("레시피를 찾을 수 없습니다. id=" + recipeId);
    }

    @Transactional
    public void reorderRecipes(RecipeReorderDto dto) {
        if (dto.getOrders() == null || dto.getOrders().isEmpty()) return;
        if (dto.getCatKey() == null || dto.getCatKey().isBlank()) return;
        recipeMapper.upsertRecipeOrders(dto.getCatKey(), dto.getOrders());
    }

    @Transactional
    public void reorderCategories(CategoryReorderDto dto) {
        if (dto.getOrders() == null || dto.getOrders().isEmpty()) return;
        recipeMapper.upsertCategoryOrders(dto.getOrders());
    }

    @Transactional
    public void deleteRecipe(Long recipeId) {
        recipeMapper.findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found. id=" + recipeId));
        recipeMapper.deleteRecipeOrderByRecipeId(recipeId);
        recipeMapper.deleteStepsByRecipeId(recipeId);
        recipeMapper.deleteById(recipeId);
    }
}
