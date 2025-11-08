package ymsoft.springdeveloper.com.springdeveloper.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleGenerateRequest {

    /** 근로자 ID */
    @NotNull(message = "memberId는 필수입니다.")
    private Long memberId;

    /** 생성 범위 시작일 (예: 현재 달의 1일) */
    @NotNull(message = "from(시작일)은 필수입니다.")
    private LocalDate from;

    /** 생성 범위 종료일 (예: N개월 후의 말일) */
    @NotNull(message = "to(종료일)은 필수입니다.")
    private LocalDate to;

    /** 생성할 일정 목록 */
    @NotNull
    @Size(min = 1, message = "생성할 일정(items)은 1개 이상이어야 합니다.")
    private List<Item> items;

    /** 중복 시 덮어쓰기 여부 (기본 false) */
    @Builder.Default
    private boolean overwrite = false;

    /** 일정 한 건 */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {

        /** 근무 날짜 */
        @NotNull(message = "date는 필수입니다.")
        private LocalDate date;

        /** 시작 시각 */
        @NotNull(message = "start는 필수입니다.")
        private LocalTime start;

        /** 종료 시각 */
        @NotNull(message = "end는 필수입니다.")
        private LocalTime end;

        /** 비고/메모 (선택) */
        private String note;
    }
}
