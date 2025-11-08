package ymsoft.springdeveloper.com.springdeveloper.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScheduleDayUpdateRequest {

    @NotNull(message = "memberId는 필수입니다.")
    private Long memberId;

    /** YYYY-MM-DD */
    @NotNull(message = "date는 필수입니다.")
    private LocalDate date;

    @NotNull @Size(min = 0, message = "segments 배열은 필수입니다.")
    private List<Segment> segments;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Segment {
        @NotNull(message = "start는 필수입니다.")
        private LocalTime start;

        @NotNull(message = "end는 필수입니다.")
        private LocalTime end;

        /** 선택 메모 */
        private String note;
    }
}
