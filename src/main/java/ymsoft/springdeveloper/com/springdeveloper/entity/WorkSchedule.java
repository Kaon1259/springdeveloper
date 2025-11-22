package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "work_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_work_schedule_member_date_start_end",
                        columnNames = {"member_id", "work_date", "start_time", "end_time"}
                )
        },
        indexes = {
                @Index(name = "idx_work_schedule_member_date", columnList = "member_id, work_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 근로자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_work_schedule_member"))
    private Member member;

    /** 실제 근무 일자 (YYYY-MM-DD) */
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    /** 시작/종료 시각 (로컬타임) */
    @Column(name = "start_time", nullable = false)
    private LocalTime start;

    @Column(name = "end_time", nullable = false)
    private LocalTime end;

    /** 생성 출처 (자동 생성/수동 입력 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20, nullable = false)
    @Builder.Default
    private SourceType source = SourceType.GENERATED;

    /** 비고(선택) */
    @Column(name = "note", length = 500)
    private String note;

    /** 생성/수정 일시 */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 편의: 분 단위 근무시간 */
    @Transient
    public int getMinutes() {
        if (start == null || end == null) return 0;
        return (end.getHour() * 60 + end.getMinute()) - (start.getHour() * 60 + start.getMinute());
    }

    public enum SourceType {
        GENERATED,   // 템플릿 기반 자동 생성
        MANUAL,      // 화면에서 수동 입력
        IMPORTED     // 외부/엑셀 등 가져오기
    }
}
