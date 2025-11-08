package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ScheduleRangeResponse {
    @Builder.Default
    private List<Day> days = new ArrayList<>();

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Day {
        private LocalDate date;
        @Builder.Default
        private List<Seg> segments = new ArrayList<>();
        private Integer minutes; // 해당 일자 총 분
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Seg {
        private LocalTime start;
        private LocalTime end;
        private String note; // 선택
    }
}
