package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(nullable = false)
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

    /** 은행명 */
    @Column
    private String bankName;

    /** 계좌 */
    @Column
    private String bankAccount;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.WORKING;

    /** ====== [추가] 급여지급일 / 주휴수당 적용 여부 ====== */
    /**
     * 급여지급일 코드
     * - "EOM" = 말일
     * - "1" ~ "31" = 해당 일자
     *
     * 프런트에서 select name="payday" 로 내려온 값을 그대로 저장합니다.
     */
    @Column(length = 8)
    @Builder.Default
    private String payday = null;   // 미지정 가능

    /**
     * 주휴수당 적용 여부 (기본 미적용)
     * 프런트의 체크박스 name="includeWeeklyHolidayAllowance" 와 매핑됩니다.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean includeWeeklyHolidayAllowance = false;

    /** 세금 적용 여부 */
    @Column(nullable = false)
    private Boolean applyTax = false;

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
        WAITING, WORKING, RESTING, PAUSED, RESIGNED;

        public static Status from(String value) {
            if (value == null || value.trim().isEmpty()) {
                return WORKING;
            }
            try {
                return Status.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return WAITING;
            }
        }

        // Status → 한글 라벨
        public static String label(Status status) {
            if (status == null) return "-";

            return switch (status) {
                case WORKING  -> "근무중";
                case WAITING  -> "시작전";
                case RESTING  -> "휴식중";
                case PAUSED   -> "일시중지";
                case RESIGNED -> "퇴사";
            };
        }

        // String status → 한글 라벨 (오버로드)
        public static String label(String value) {
            return label(from(value));
        }
    }

    /* ================= 유틸 ================= */

    /** 급여지급일 표시용 라벨 (예: "말일" 또는 "15일", 미지정 시 "-") */
    public String getPaydayLabel() {
        if (payday == null || payday.isBlank()) return "-";
        if ("EOM".equalsIgnoreCase(payday)) return "말일";
        return payday + "일";
    }

    /** 주휴수당 적용 여부(표시용) */
    public String getWeeklyHolidayAllowanceLabel() {
        return Boolean.TRUE.equals(includeWeeklyHolidayAllowance) ? "적용" : "미적용";
    }
    
    public  String getApplyTaxLabel() {
        return Boolean.TRUE.equals(applyTax) ? "적용" : "미적용";
    }
}
