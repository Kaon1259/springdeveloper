package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FavoriteReorderDto {
    private List<OrderItem> orders;

    @Getter
    @Setter
    public static class OrderItem {
        private Long id;
        private int position;
    }
}
