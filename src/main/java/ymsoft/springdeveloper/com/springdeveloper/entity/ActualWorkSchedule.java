package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "actual_work_schedule",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_member_date", columnNames = {"member_id", "work_date"})
        },
        indexes = {
                @Index(name = "idx_member_date", columnList = "member_id, work_date")
        }
)
public class ActualWorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    /** JSON 문자열: 프론트 payload를 그대로 직렬화해 저장 */
    @Column(name = "segments_json", columnDefinition = "json", nullable = false)
    private String segmentsJson;

    @Column(name = "total_minutes", nullable = false)
    private Integer totalMinutes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
