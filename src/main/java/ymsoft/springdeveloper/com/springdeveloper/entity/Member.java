package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "schedules")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이름 */
    @Column(nullable = false)
    private String name;

    /** 성별 */
    @Column(length = 10)
    private String gender;

    /** 아르바이트 시작일 */
    @Column(nullable = false)
    private LocalDate startDate;

    /** 연락처/이메일/시급/보건증 만료일 */
    @Column(nullable = false, unique = false)
    private String phone;

    @Column
    private String email;

    @Column(nullable = false)
    private Integer hourlyWage; // 원 단위 정수

    @Column
    private LocalDate healthCertExpiry;

    /** 보건증 보유 여부 */
    @Column(nullable = false)
    private Boolean hasHealthCertificate = false;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.WORKING;

    /** 데이터 등록/수정일 */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /** 스케줄 */
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ScheduleItem> schedules = new ArrayList<>();

    public enum Status {
        WAITING, WORKING, RESTING, PAUSED, RESIGNED
    }

    @Override
    public String toString() {
        return "Member [id=" + id + ", name=" + name + ", gender=" + gender + ", startDate="  + startDate + ", phone=" + phone + ", email=" + email +
                ", hourlyWage=" + hourlyWage + ", healthCertExpiry=" + healthCertExpiry + ", status=" + status + ", schedules=" + schedules.toString() + "]";
    }



}
