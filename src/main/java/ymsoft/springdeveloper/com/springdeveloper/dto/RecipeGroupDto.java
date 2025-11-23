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
public class RecipeGroupDto {

    // 화면에 표시할 카테고리 라벨 (예: "Hot 커피")
    private String categoryLabel;

    // 이 카테고리에 속한 레시피들
    private List<RecipeViewDto> recipes;
}
