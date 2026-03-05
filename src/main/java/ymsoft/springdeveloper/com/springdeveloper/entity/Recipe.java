package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.enums.CupSize;
import ymsoft.springdeveloper.com.springdeveloper.enums.RecipeCategory;
import ymsoft.springdeveloper.com.springdeveloper.enums.Temperature;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    private Long id;
    private String menuName;
    private String author;
    private String description;
    private String imgname;
    private boolean visible = false;
    private boolean template = false;
    private boolean useBlender = false;
    private Temperature temperature;
    private CupSize cupSize;
    private RecipeCategory category;
    @Builder.Default
    private List<RecipeStep> steps = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void addStep(RecipeStep step) {
        steps.add(step);
        step.setRecipe(this);
    }

    public void removeStep(RecipeStep step) {
        steps.remove(step);
        step.setRecipe(null);
    }
}
