package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WorkSchedule {

    private Long id;
    private Long memberId;
    private Member member;
    private LocalDate workDate;
    private LocalTime start;
    private LocalTime end;
    @Builder.Default
    private SourceType source = SourceType.GENERATED;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getMinutes() {
        if (start == null || end == null) return 0;
        return (end.getHour() * 60 + end.getMinute()) - (start.getHour() * 60 + start.getMinute());
    }

    public enum SourceType {
        GENERATED, MANUAL, IMPORTED
    }
}
