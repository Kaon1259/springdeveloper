package ymsoft.springdeveloper.com.springdeveloper.repository;


import org.springframework.data.jpa.repository.JpaRepository;
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
}
