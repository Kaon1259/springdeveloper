package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

// entity/ScheduleItem.java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum WeekDay { MON, TUE, WED, THU, FRI, SAT, SUN }

    @Enumerated(EnumType.STRING)
    @Column(name="day_name", length = 3, nullable = false)
    private WeekDay day;

    @Column(name = "start_time", nullable = false)
    private LocalTime start;

    @Column(name = "end_time", nullable = false)
    private LocalTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Override
    public String toString() {
        return  "[ WeekDay: " + day + ", Start: " + start + ", End: " + end + ", Member: " + member + "]";
    }
}
