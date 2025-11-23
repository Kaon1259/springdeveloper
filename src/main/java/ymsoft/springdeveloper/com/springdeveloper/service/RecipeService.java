package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeCreateRequestDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
import ymsoft.springdeveloper.com.springdeveloper.repository.RecipeRepository;

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
}
