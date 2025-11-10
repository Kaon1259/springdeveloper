package ymsoft.springdeveloper.com.springdeveloper.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ymsoft.springdeveloper.com.springdeveloper.dto.YearlyPayrollDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;

import java.util.List;
import java.util.Optional;

public interface PayrollMonthRepository extends JpaRepository<PayrollMonth, Long> {

    Optional<PayrollMonth> findByMemberIdAndPayYearAndPayMonth(Long memberId, Integer payYear, Integer payMonth);

    List<PayrollMonth> findByMemberIdOrderByPayYearDescPayMonthDesc(Long memberId);

    List<PayrollMonth> findByMemberIdAndStatusOrderByPayYearDescPayMonthDesc(
            Long memberId,
            PayrollStatus status
    );

    // 이미 있으실 수도 있는 메서드 (월별)
    List<PayrollMonth> findByPayYearAndPayMonth(int payYear, int payMonth);

    /**
     * 특정 연도 기준 근무자별 연간 합계
     * - 각 멤버별로 월별 데이터 합산
     * - 상태 필터가 필요하면 where 절에 조건 추가 가능 (예: AND pm.status = 'PAID')
     */
    @Query("""
        select new ymsoft.springdeveloper.com.springdeveloper.dto.YearlyPayrollDto(
            m.id,
            m.name,
            m.phone,
            sum(pm.monthWorkMinutes),
            sum(pm.monthJuhyuMinutes),
            avg(pm.hourlyWage),
            sum(pm.monthWorkPay),
            sum(pm.monthJuhyuPay),
            sum(pm.monthWorkPay + pm.monthJuhyuPay)
        )
        from PayrollMonth pm
        join pm.member m
        where pm.payYear = :year
        group by m.id, m.name, m.phone
        order by m.name
        """)
    List<YearlyPayrollDto> findYearlySummaryByYear(@Param("year") int year);
}
