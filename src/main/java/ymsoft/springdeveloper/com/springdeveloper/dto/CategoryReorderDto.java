package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CategoryReorderDto {
    private List<OrderItem> orders;

    @Getter
    @Setter
    public static class OrderItem {
        private String categoryName;
        private int sortOrder;
    }
}
