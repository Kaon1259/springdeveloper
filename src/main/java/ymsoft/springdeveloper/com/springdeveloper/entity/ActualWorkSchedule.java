package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActualWorkSchedule {

    private Long id;
    private Long memberId;
    private LocalDate workDate;
    private String segmentsJson;
    private Integer totalMinutes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
