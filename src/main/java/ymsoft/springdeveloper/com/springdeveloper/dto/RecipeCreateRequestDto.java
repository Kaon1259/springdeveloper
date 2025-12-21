package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
import ymsoft.springdeveloper.com.springdeveloper.entity.RecipeStep;
import ymsoft.springdeveloper.com.springdeveloper.enums.CupSize;
import ymsoft.springdeveloper.com.springdeveloper.enums.RecipeCategory;
import ymsoft.springdeveloper.com.springdeveloper.enums.Temperature;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeCreateRequestDto {

    private Long id;
    private String menuName;
    private String author;
    private String description;
    private String imgname;

    private boolean visible;
    private boolean template;
    private boolean useBlender;

    /**🔥 이제 Enum 타입으로 직접 받는다 */
    private Temperature temperature;        // HOT / ICE
    private CupSize cupSize;               // OZ20, OZ24, OZ32
    private RecipeCategory category;       // HOT_COFFEE … FRAPPE

    /** Step 텍스트 리스트 */
    private List<String> steps;
    private List<Integer> stepTimes;

    /** DTO → Entity 변환 */
    public Recipe toEntity() {

        Recipe recipe = Recipe.builder()
                .menuName(menuName)
                .author(author)
                .description(description)
                .imgname(imgname)
                .temperature(temperature)
                .visible(Boolean.TRUE.equals(visible))
                .template(Boolean.TRUE.equals(template))
                .useBlender(Boolean.TRUE.equals(useBlender))
                .cupSize(cupSize)
                .category(category)
                .build();

        /** Step 엔티티 변환 */
        List<RecipeStep> stepEntities = new ArrayList<>();
        if (steps != null) {
            for (int i = 0; i < steps.size(); i++) {
                String content = steps.get(i);
                if (content == null || content.isBlank()) continue;

                RecipeStep step = RecipeStep.builder()
                        .recipe(recipe)
                        .stepOrder(i + 1)
                        .stepTime(stepTimes.get(i))
                        .content(content.trim())
                        .build();

                stepEntities.add(step);
            }
        }

        recipe.setSteps(stepEntities);

        return recipe;
    }

    public static RecipeCreateRequestDto toForm(Recipe recipe) {
        return RecipeCreateRequestDto.builder()
                .id(recipe.getId())
                .menuName(recipe.getMenuName())
                .author(recipe.getAuthor())
                .description(recipe.getDescription())
                .imgname(recipe.getImgname() == null ? "" : recipe.getImgname())
                .visible(recipe.isVisible())
                .template(recipe.isTemplate())
                .useBlender(recipe.isUseBlender())
                .temperature(recipe.getTemperature())
                .cupSize(recipe.getCupSize())
                .category(recipe.getCategory())
                .steps(
                        recipe.getSteps() == null ? null :
                                recipe.getSteps().stream()
                                        .sorted(Comparator.comparing(RecipeStep::getStepOrder))
                                        .map(RecipeStep::getContent)
                                        .collect(Collectors.toList())
                )
                .stepTimes(
                        recipe.getSteps().stream()
                                .sorted(Comparator.comparing(RecipeStep::getStepOrder))
                                .map(RecipeStep::getStepTime)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static List<RecipeCreateRequestDto> toDto(List<Recipe> recipes) {
        return recipes.stream()
            .map(RecipeCreateRequestDto::toForm)
            .collect(Collectors.toList());
    }
}
