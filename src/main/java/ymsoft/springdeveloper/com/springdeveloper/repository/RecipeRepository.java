package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
