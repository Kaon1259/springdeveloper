package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MonthlyPayrollDto {
    private Long memberId;
    private String memberName;
    private String memberPhone;
    private String memberCode;

    private int workMinutes;     // 실근무 분
    private int juhyuMinutes;    // 주휴수당 분
    private int hourlyWage;      // 시급
    private int workPay;         // 실근무 급여
    private int juhyuPay;        // 주휴 수당
    private int totalPay;        // 총액

    private String status;       // DRAFT / CONFIRMED / PAID
    private String createdAt;
    private String savedAt;
    private String confirmedAt;
    private String paidAt;
}
