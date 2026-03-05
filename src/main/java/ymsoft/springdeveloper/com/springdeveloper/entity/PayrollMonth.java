package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.time.LocalDateTime;

@ToString
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayrollMonth {

    private Long id;
    private Long memberId;
    private Member member;
    private Integer payYear;
    private Integer payMonth;
    private Integer hourlyWage;
    private Integer monthWorkMinutes;
    private Integer monthJuhyuMinutes;
    private Long monthWorkPay;
    private Long monthJuhyuPay;
    private Long monthTotalPay;
    @Builder.Default
    private PayrollStatus status = PayrollStatus.DRAFT;
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
