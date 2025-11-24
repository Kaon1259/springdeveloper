package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByCategoryAscMenuNameAsc();

    Recipe findById(long id);

    List<Recipe> findByVisibleTrueOrderByUpdatedAtDesc();
}
