package ymsoft.springdeveloper.com.springdeveloper.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuDto {

    private Long id;            // 메뉴 ID
    private String name;        // 메뉴명 ex) "아메리카노"
    private String category;    // 카테고리 ex) "커피", "라떼", "스무디"
    private String hotOrIced;   // HOT/ICED 구분 ex) "HOT", "ICED", "HOT/ICED"
    private boolean isSignature; // 시그니처 메뉴 여부
}
