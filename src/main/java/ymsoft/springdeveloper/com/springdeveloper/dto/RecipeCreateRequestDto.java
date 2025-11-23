package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
import ymsoft.springdeveloper.com.springdeveloper.entity.RecipeStep;
import ymsoft.springdeveloper.com.springdeveloper.enums.CupSize;
import ymsoft.springdeveloper.com.springdeveloper.enums.RecipeCategory;
import ymsoft.springdeveloper.com.springdeveloper.enums.Temperature;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeCreateRequestDto {

    private String menuName;
    private String author;
    private String description;

    /**🔥 이제 Enum 타입으로 직접 받는다 */
    private Temperature temperature;        // HOT / ICE
    private CupSize cupSize;               // OZ20, OZ24, OZ32
    private RecipeCategory category;       // HOT_COFFEE … FRAPPE

    /** Step 텍스트 리스트 */
    private List<String> steps;

    /** DTO → Entity 변환 */
    public Recipe toEntity() {

        Recipe recipe = Recipe.builder()
                .menuName(menuName)
                .author(author)
                .description(description)
                .temperature(temperature)
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
                        .content(content.trim())
                        .build();

                stepEntities.add(step);
            }
        }

        recipe.setSteps(stepEntities);

        return recipe;
    }
}
