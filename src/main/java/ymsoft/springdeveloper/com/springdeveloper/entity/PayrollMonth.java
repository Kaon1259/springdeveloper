package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.time.LocalDateTime;

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

    @Column(name = "member_id", nullable = false)
    private Long memberId;

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

    // --- getter / setter 생략 가능(롬복 쓰면 @Data/@Getter 등) ---

    public Long getId() { return id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public Integer getPayYear() { return payYear; }
    public void setPayYear(Integer payYear) { this.payYear = payYear; }

    public Integer getPayMonth() { return payMonth; }
    public void setPayMonth(Integer payMonth) { this.payMonth = payMonth; }

    public Integer getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(Integer hourlyWage) { this.hourlyWage = hourlyWage; }

    public Integer getMonthWorkMinutes() { return monthWorkMinutes; }
    public void setMonthWorkMinutes(Integer monthWorkMinutes) { this.monthWorkMinutes = monthWorkMinutes; }

    public Integer getMonthJuhyuMinutes() { return monthJuhyuMinutes; }
    public void setMonthJuhyuMinutes(Integer monthJuhyuMinutes) { this.monthJuhyuMinutes = monthJuhyuMinutes; }

    public Long getMonthWorkPay() { return monthWorkPay; }
    public void setMonthWorkPay(Long monthWorkPay) { this.monthWorkPay = monthWorkPay; }

    public Long getMonthJuhyuPay() { return monthJuhyuPay; }
    public void setMonthJuhyuPay(Long monthJuhyuPay) { this.monthJuhyuPay = monthJuhyuPay; }

    public Long getMonthTotalPay() { return monthTotalPay; }
    public void setMonthTotalPay(Long monthTotalPay) { this.monthTotalPay = monthTotalPay; }

    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

