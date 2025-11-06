package ymsoft.springdeveloper.com.springdeveloper.dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class ActualWorkScheduleDto {
    private Long memberId;
    private String date;
    private List<Segment> segments;

    // inner static class for segment
    @Getter @Setter
    public static class Segment {
        private String start;
        private String end;

        @Override
        public String toString() {
            return start + " ~ " + end;
        }
    }

    public ActualWorkSchedule toEntity(){
        ObjectMapper mapper = new ObjectMapper();
        String json = "[]";

        try {
            json = mapper.writeValueAsString(segments);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        int totalMinutes = calcTotalMinutes(segments);

        return ActualWorkSchedule.builder()
                .memberId(memberId)
                .workDate(LocalDate.parse(date))
                .segmentsJson(json)
                .totalMinutes(totalMinutes)
                .build();
    }

    /** 근무 시간 총합 계산 (분 단위) */
    private int calcTotalMinutes(List<Segment> segments) {
        if (segments == null || segments.isEmpty()) return 0;
        return segments.stream()
                .mapToInt(s -> hmToMinutes(s.getEnd()) - hmToMinutes(s.getStart()))
                .sum();
    }

    private int hmToMinutes(String hm) {
        String[] parts = hm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}


