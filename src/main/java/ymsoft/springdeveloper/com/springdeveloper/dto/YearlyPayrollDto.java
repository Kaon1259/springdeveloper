package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 연도별 근무자 급여 합계 DTO
 * 프론트의 /api/payroll/yearly 응답 구조에 맞춤
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlyPayrollDto {

    private Long memberId;
    private String memberName;
    private String memberPhone;
    //private String memberCode;

    // 연간 합계값 (분/금액)
    private long workMinutes;    // 연간 실근무 분
    private long juhyuMinutes;   // 연간 주휴수당 분

    private Double hourlyWage;   // 기준 시급(평균 또는 대표값)

    private long workPay;        // 연간 실근무 급여 합계
    private long juhyuPay;       // 연간 주휴 수당 합계
    private long totalPay;       // 연간 총액 (workPay + juhyuPay)
}
