package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStepViewDto {
    private Integer stepOrder;
    private String content;
}
