package ymsoft.springdeveloper.com.springdeveloper.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleBatchUpdateReqeust {

    /**
     * 대상 아르바이트생 ID
     */
    private Long memberId;

    /**
     * 주 시작일 (월요일), "yyyy-MM-dd"
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStart;

    /**
     * 주 종료일 (일요일), "yyyy-MM-dd"
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEnd;

    /**
     * 이 주간에 대한 날짜별 계획 목록
     */
    private List<PlanDay> days = new ArrayList<>();

    /**
     * 내부 클래스: 하루 단위 계획
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanDay {

        /**
         * "yyyy-MM-dd" 형태 (예: "2025-11-17")
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        /**
         * 해당 날짜의 계획 구간 리스트
         */
        private List<Segment> segments = new ArrayList<>();
    }

    /**
     * 내부 클래스: 시간 구간 (start~end)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Segment {

        /**
         * "HH:mm" 형태 (예: "10:00")
         */
        private String start;

        /**
         * "HH:mm" 형태 (예: "14:00")
         */
        private String end;
    }
}
