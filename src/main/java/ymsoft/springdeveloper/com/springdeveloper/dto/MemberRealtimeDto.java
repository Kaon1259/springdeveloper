package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// 화면 전용 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRealtimeDto {

    private Long id;
    private String name;
    private String gender;
    private Member.Status status;

    /**
     * JS에서 getSchedules(m)로 쓰게 될 컬렉션
     *  - day   : "MON" / "TUE" ... (오늘 요일)
     *  - start : "HH:mm"
     *  - end   : "HH:mm"
     */
    private List<SlotDto> schedules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SlotDto {
        private String day;   // 예: "FRI"
        private String start; // "09:00"
        private String end;   // "13:30"
    }

    public static MemberRealtimeDto from(MemberDto memberDto,
                                         List<WorkSchedule> workSchedules,
                                         String dayKey) {

        List<SlotDto> slots = workSchedules.stream()
                .sorted(Comparator.comparing(WorkSchedule::getStart))
                .map(ws -> SlotDto.builder()
                        .day(dayKey) // 오늘만 보기 때문에 전부 같은 dayKey
                        .start(toHm(ws.getStart()))
                        .end(toHm(ws.getEnd()))
                        .build()
                )
                .collect(Collectors.toList());

        return MemberRealtimeDto.builder()
                .id(memberDto.getId())
                .name(memberDto.getName())
                .gender(memberDto.getGender())
                .status(memberDto.getStatus())
                .schedules(slots)
                .build();
    }

    private static String toHm(LocalTime t) {
        if (t == null) return "00:00";
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }
}
