package ymsoft.springdeveloper.com.springdeveloper.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymsoft.springdeveloper.com.springdeveloper.dto.FavoriteReorderDto;
import ymsoft.springdeveloper.com.springdeveloper.service.RecipeService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cafe")
public class CafeApiController {

    private final RecipeService recipeService;

    @PostMapping("/recipe/{recipeId}/visible/{visible}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> updateVisible(@PathVariable("recipeId") Long recipeId,
                        @PathVariable("visible") boolean visible) {

        log.info("Updating visible... " + recipeId + ":" +visible);
        recipeService.updateVisible(recipeId, visible);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recipe/{recipeId}/template/{template}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> updateTemplate(@PathVariable("recipeId") Long recipeId,
                                           @PathVariable("template") boolean template) {

        log.info("Updating visible... " + recipeId + ":" +template);
        recipeService.updateTemplate(recipeId, template);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recipe/{recipeId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> deleteRecipe(@PathVariable("recipeId") Long recipeId) {

        log.info("delete recipe... " + recipeId);
        recipeService.deleteRecipe(recipeId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/favorites/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<?> reorderFavorites(@RequestBody FavoriteReorderDto dto) {
        log.info("reorder favorites... {}개", dto.getOrders() != null ? dto.getOrders().size() : 0);
        recipeService.reorderFavorites(dto);
        return ResponseEntity.noContent().build();
    }
}
