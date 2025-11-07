package ymsoft.springdeveloper.com.springdeveloper.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor               // ✅ 기본 생성자
@AllArgsConstructor              // (선택) 필요 시
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActualWorkScheduleBulkDto {

    /** 공통 근무일(YYYY-MM-DD) */
    @NotBlank
    private String date;

    /** 멤버별 실제 근무 세그먼트 묶음 */
    @JsonProperty("segments")     // ✅ 상위 JSON 배열명과 명시적으로 매핑
    private List<Item> segments;

    @Getter @Setter
    @NoArgsConstructor           // ✅ 기본 생성자
    @AllArgsConstructor          // (선택)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @NotNull
        private Long memberId;

        /** ActualWorkScheduleDto.Segment 와 동일한 필드 구조 사용 */
        @JsonProperty("segments") // ✅ 내부도 명시 매핑
        private List<ActualWorkScheduleDto.Segment> segments;
    }
}
