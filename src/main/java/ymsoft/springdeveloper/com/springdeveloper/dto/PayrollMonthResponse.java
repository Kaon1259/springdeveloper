package ymsoft.springdeveloper.com.springdeveloper.dto;


import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.time.LocalDateTime;

public class PayrollMonthResponse {

    private Long id;

    private Long memberId;
    private Integer payYear;
    private Integer payMonth;

    private Integer hourlyWage;

    private Integer monthWorkMinutes;
    private Integer monthJuhyuMinutes;

    private Long monthWorkPay;
    private Long monthJuhyuPay;
    private Long monthTotalPay;

    private String status;         // "DRAFT" / "CONFIRMED" / "PAID"
    private LocalDateTime confirmedAt;
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // --- 생성자 & 팩토리 메서드 ---

    public static PayrollMonthResponse fromEntity(PayrollMonth entity) {
        PayrollMonthResponse res = new PayrollMonthResponse();
        res.id = entity.getId();
        res.memberId = entity.getMember().getId(); //getMemberId();
        res.payYear = entity.getPayYear();
        res.payMonth = entity.getPayMonth();
        res.hourlyWage = entity.getHourlyWage();
        res.monthWorkMinutes = entity.getMonthWorkMinutes();
        res.monthJuhyuMinutes = entity.getMonthJuhyuMinutes();
        res.monthWorkPay = entity.getMonthWorkPay();
        res.monthJuhyuPay = entity.getMonthJuhyuPay();
        res.monthTotalPay = entity.getMonthTotalPay();
        PayrollStatus status = entity.getStatus();
        res.status = (status != null) ? status.name() : null;
        res.confirmedAt = entity.getConfirmedAt();
        res.paidAt = entity.getPaidAt();
        res.createdAt = entity.getCreatedAt();
        res.updatedAt = entity.getUpdatedAt();
        return res;
    }

    // --- getter (필요하면 setter도 추가) ---

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public Integer getPayYear() { return payYear; }
    public Integer getPayMonth() { return payMonth; }
    public Integer getHourlyWage() { return hourlyWage; }
    public Integer getMonthWorkMinutes() { return monthWorkMinutes; }
    public Integer getMonthJuhyuMinutes() { return monthJuhyuMinutes; }
    public Long getMonthWorkPay() { return monthWorkPay; }
    public Long getMonthJuhyuPay() { return monthJuhyuPay; }
    public Long getMonthTotalPay() { return monthTotalPay; }
    public String getStatus() { return status; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

