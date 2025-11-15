package ymsoft.springdeveloper.com.springdeveloper.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
public class PayrollMonthRequest {

    private Long memberId;
    private Integer payYear;
    private Integer payMonth;

    private Integer hourlyWage;

    private Integer monthWorkMinutes;
    private Integer monthJuhyuMinutes;

    private Long monthWorkPay;
    private Long monthJuhyuPay;
    private Long monthTotalPay;

    private String status; // "DRAFT" / "CONFIRMED" / "PAID"

}
