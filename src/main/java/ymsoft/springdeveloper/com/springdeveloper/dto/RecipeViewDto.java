package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeViewDto {

    private Long id;
    private String menuName;

    private boolean visible;
    private boolean template;

    // Enum name 그대로 (HOT_COFFEE, ICE_COFFEE...)
    private String category;

    // HOT / ICE
    private String temperature;

    private List<RecipeStepViewDto> steps;
}
