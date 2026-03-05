package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.dto.YearlyPayrollDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PayrollMonthMapper {
    Optional<PayrollMonth> findByMemberIdAndPayYearAndPayMonth(@Param("memberId") Long memberId, @Param("payYear") Integer payYear, @Param("payMonth") Integer payMonth);
    List<PayrollMonth> findByMemberIdOrderByPayYearDescPayMonthDesc(@Param("memberId") Long memberId);
    List<PayrollMonth> findByMemberIdAndStatusOrderByPayYearDescPayMonthDesc(@Param("memberId") Long memberId, @Param("status") String status);
    List<PayrollMonth> findByPayYearAndPayMonth(@Param("payYear") int payYear, @Param("payMonth") int payMonth);
    List<YearlyPayrollDto> findYearlySummaryByYear(@Param("year") int year);
    void insert(PayrollMonth payrollMonth);
    void update(PayrollMonth payrollMonth);
}
