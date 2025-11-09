package ymsoft.springdeveloper.com.springdeveloper.dto;


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

    // --- getter / setter ---

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
