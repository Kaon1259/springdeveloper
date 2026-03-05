package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItem {

    private Long id;
    private Long memberId;
    private Member member;

    public enum WeekDay { MON, TUE, WED, THU, FRI, SAT, SUN }

    private WeekDay day;
    private LocalTime start;
    private LocalTime end;

    @Override
    public String toString() {
        return "[ WeekDay: " + day + ", Start: " + start + ", End: " + end + ", MemberId: " + memberId + "]";
    }
}
