package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecipeReorderDto {
    private String catKey;
    private List<OrderItem> orders;

    @Getter
    @Setter
    public static class OrderItem {
        private Long id;
        private int sortOrder;
    }
}
