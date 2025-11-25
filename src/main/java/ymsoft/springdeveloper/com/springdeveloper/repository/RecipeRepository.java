package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByOrderByCategoryAscMenuNameAsc();

    Recipe findById(long id);

    List<Recipe> findByVisibleTrueOrderByUpdatedAtDesc();

    List<Recipe> findAllByTemplateTrueOrderByCategoryAscMenuNameAsc();


    @Modifying
    @Query("update Recipe r set r.visible = :visible where r.id = :id")
    int updateVisible(@Param("id") Long id, @Param("visible") boolean visible);

    @Modifying
    @Query("update Recipe r set r.template = :template where r.id = :id")
    int updateTemplate(@Param("id") Long id, @Param("template") boolean template);

}
