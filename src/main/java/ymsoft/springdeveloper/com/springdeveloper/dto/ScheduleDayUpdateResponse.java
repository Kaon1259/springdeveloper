package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScheduleDayUpdateResponse {

    private Long memberId;
    private LocalDate date;

    /** 저장된 구간 수 */
    private int count;

    /** 총 분(min) */
    private int minutes;

    private List<Segment> segments;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Segment {
        private String start; // "HH:mm"
        private String end;   // "HH:mm"
        private String note;
    }
}
