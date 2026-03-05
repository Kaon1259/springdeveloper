package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "schedules")
public class Member {

    private Long id;
    private String name;
    private String gender;
    private LocalDate startDate;
    private String phone;
    private String email;
    private String ssn;
    private Integer hourlyWage;
    private Integer hourlyWage2;
    private LocalDate healthCertExpiry;
    @Builder.Default
    private Boolean hasHealthCertificate = false;
    private String bankName;
    private String bankAccount;
    @Builder.Default
    private Status status = Status.WORKING;
    private String memo;
    @Builder.Default
    private String payday = null;
    @Builder.Default
    private Boolean includeWeeklyHolidayAllowance = false;
    private Boolean applyTax = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ScheduleItem> schedules = new ArrayList<>();

    public enum Status {
        WAITING, WORKING, RESTING, PAUSED, RESIGNED;

        public static Status from(String value) {
            if (value == null || value.trim().isEmpty()) return WORKING;
            try {
                return Status.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return WAITING;
            }
        }

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

        public static String label(String value) {
            return label(from(value));
        }
    }

    public String getPaydayLabel() {
        if (payday == null || payday.isBlank()) return "-";
        if ("EOM".equalsIgnoreCase(payday)) return "말일";
        return payday + "일";
    }

    public String getWeeklyHolidayAllowanceLabel() {
        return Boolean.TRUE.equals(includeWeeklyHolidayAllowance) ? "적용" : "미적용";
    }

    public String getApplyTaxLabel() {
        return Boolean.TRUE.equals(applyTax) ? "적용" : "미적용";
    }
}
