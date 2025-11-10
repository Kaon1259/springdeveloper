package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "PAYROLL_MONTH",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_payroll_month", columnNames = {"member_id", "pay_year", "pay_month"})
        }
)
public class PayrollMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(name = "member_id", nullable = false)
//    private Long memberId;

    @Column(name = "pay_year", nullable = false)
    private Integer payYear;

    @Column(name = "pay_month", nullable = false)
    private Integer payMonth;

    @Column(name = "hourly_wage", nullable = false)
    private Integer hourlyWage;

    @Column(name = "month_work_minutes", nullable = false)
    private Integer monthWorkMinutes;

    @Column(name = "month_juhyu_minutes", nullable = false)
    private Integer monthJuhyuMinutes;

    @Column(name = "month_work_pay", nullable = false)
    private Long monthWorkPay;

    @Column(name = "month_juhyu_pay", nullable = false)
    private Long monthJuhyuPay;

    @Column(name = "month_total_pay", nullable = false)
    private Long monthTotalPay;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}

