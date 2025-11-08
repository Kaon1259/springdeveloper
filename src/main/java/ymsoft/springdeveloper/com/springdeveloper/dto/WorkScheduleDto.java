package ymsoft.springdeveloper.com.springdeveloper.dto;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkScheduleDto {

    private Long id;
    private Long memberId;
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
    private int minutes;
    private String source; // GENERATED / MANUAL / IMPORTED
    private String note;

    public static WorkScheduleDto fromEntity(WorkSchedule e) {
        return WorkScheduleDto.builder()
                .id(e.getId())
                .memberId(e.getMember() != null ? e.getMember().getId() : null)
                .date(e.getWorkDate())
                .start(e.getStart())
                .end(e.getEnd())
                .minutes(e.getMinutes())
                .source(e.getSource() != null ? e.getSource().name() : null)
                .note(e.getNote())
                .build();
    }
}
