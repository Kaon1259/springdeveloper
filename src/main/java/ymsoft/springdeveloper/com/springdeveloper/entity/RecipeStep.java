package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {

    private Long id;
    private Long recipeId;
    private Recipe recipe;
    private Integer stepOrder;
    private Integer stepTime = 0;
    private String content;
}
