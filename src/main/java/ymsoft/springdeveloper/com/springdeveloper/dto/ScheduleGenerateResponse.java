package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleGenerateResponse {

    /** 요청된 총 항목 수 */
    private int totalRequested;

    /** 실제 생성된 일정 수 */
    private int created;

    /** 건너뛴(중복 등) 일정 수 */
    private int skipped;

    /** 덮어쓰기된 일정 수 (overwrite=true일 경우) */
    private int overwritten;

    /** 새로 생성된 WorkSchedule ID 목록 */
    @Builder.Default
    private List<Long> createdIds = new ArrayList<>();

    /** 충돌(중복 등) 상세 내역 */
    @Builder.Default
    private List<Conflict> conflicts = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Conflict {

        /** 날짜 (YYYY-MM-DD) */
        private String date;

        /** 시작 시각 (HH:mm) */
        private String start;

        /** 종료 시각 (HH:mm) */
        private String end;

        /** 기존 일정 ID (겹치는 경우) */
        private Long existingId;

        /** 이유 코드 (DUPLICATED, INVALID_10MIN 등) */
        private String reason;
    }
}

