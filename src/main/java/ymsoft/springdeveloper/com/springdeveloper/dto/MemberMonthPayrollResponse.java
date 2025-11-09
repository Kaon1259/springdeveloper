package ymsoft.springdeveloper.com.springdeveloper.dto;


import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberMonthPayrollResponse {

    private Long id;

    private Integer payYear;
    private Integer payMonth;
    private String status;          // "PAID", "DRAFT", "CONFIRMED"

    private Integer hourlyWage;

    private Integer monthWorkMinutes;
    private Integer monthJuhyuMinutes;

    private Long monthWorkPay;
    private Long monthJuhyuPay;
    private Long monthTotalPay;

    private LocalDateTime paidDate;     // "yyyy-MM-dd" 형식으로 JSON 출력

//    public MemberMonthPayrollResponse() {
//    }

//    public MemberMonthPayrollResponse(Long id, Integer payYear, Integer payMonth, String status,
//                                      Integer hourlyWage,
//                                      Integer monthWorkMinutes, Integer monthJuhyuMinutes,
//                                      Long monthWorkPay, Long monthJuhyuPay, Long monthTotalPay,
//                                      LocalDateTime paidDate) {
//        this.id = id;
//        this.payYear = payYear;
//        this.payMonth = payMonth;
//        this.status = status;
//        this.hourlyWage = hourlyWage;
//        this.monthWorkMinutes = monthWorkMinutes;
//        this.monthJuhyuMinutes = monthJuhyuMinutes;
//        this.monthWorkPay = monthWorkPay;
//        this.monthJuhyuPay = monthJuhyuPay;
//        this.monthTotalPay = monthTotalPay;
//        this.paidDate = paidDate;
//    }

    public static MemberMonthPayrollResponse from(PayrollMonth payroll) {
        if (payroll == null) return null;
        return new MemberMonthPayrollResponse(
                payroll.getId(),
                payroll.getPayYear(),
                payroll.getPayMonth(),
                payroll.getStatus() != null ? payroll.getStatus().name() : null,
                payroll.getHourlyWage(),
                payroll.getMonthWorkMinutes(),
                payroll.getMonthJuhyuMinutes(),
                payroll.getMonthWorkPay(),
                payroll.getMonthJuhyuPay(),
                payroll.getMonthTotalPay(),
                payroll.getPaidAt()
        );
    }

    // getter, setter 전부 추가
}
