package ymsoft.springdeveloper.com.springdeveloper.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkScheduleRequest {

    @NotNull
    private Long memberId;

    @NotNull
    private LocalDate date;   // JSON: "2025-01-01"

    /**
     * 비어 있으면 휴무로 간주 (totalMinutes = 0, segmentsJson = "[]")
     */
    @NotNull
    private List<SegmentDto> segments;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentDto {
        // "HH:mm" 형식 (예: "09:00")
        @NotNull
        private String start;

        @NotNull
        private String end;

        @Override
        public String toString() {
            return "SegmentDto{start='" + start + "', end='" + end + "'}";
        }
    }
}
