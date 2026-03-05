package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.dto.CategoryReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.FavoriteReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.RecipeReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Recipe;
import ymsoft.springdeveloper.com.springdeveloper.entity.RecipeStep;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface RecipeMapper {
    List<Recipe> findAllOrderByCategoryMenuName();
    Optional<Recipe> findById(@Param("id") Long id);
    List<Recipe> findByVisibleTrueOrderByUpdatedAtDesc();
    List<Recipe> findByVisibleTrueOrderByFavoritePosition();
    List<Recipe> findAllByTemplateTrueOrderByCategoryMenuName();
    int updateVisible(@Param("id") Long id, @Param("visible") boolean visible);
    int updateTemplate(@Param("id") Long id, @Param("template") boolean template);
    void insert(Recipe recipe);
    void update(Recipe recipe);
    void deleteById(@Param("id") Long id);
    void insertStep(RecipeStep step);
    void deleteStepsByRecipeId(@Param("recipeId") Long recipeId);
    List<RecipeStep> findStepsByRecipeId(@Param("recipeId") Long recipeId);

    // 카테고리별 레시피 순서 (recipe_order 테이블)
    void upsertRecipeOrders(@Param("catKey") String catKey,
                            @Param("orders") List<RecipeReorderDto.OrderItem> orders);
    void deleteRecipeOrderByRecipeId(@Param("recipeId") Long recipeId);

    // 카테고리 순서 (recipe_category_order 테이블)
    List<Map<String, Object>> findAllCategoryOrders();
    void upsertCategoryOrders(@Param("list") List<CategoryReorderDto.OrderItem> list);

    // 즐겨찾기 순서 (favorite_order 테이블)
    void upsertFavoriteOrders(@Param("list") List<FavoriteReorderDto.OrderItem> list);
}
