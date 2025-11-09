package ymsoft.springdeveloper.com.springdeveloper.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberMonthPayrollResponse;
import ymsoft.springdeveloper.com.springdeveloper.dto.PayrollMonthRequest;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;
import ymsoft.springdeveloper.com.springdeveloper.service.PayrollMonthService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payroll")
public class PayrollMonthController {

    @Autowired
    private PayrollMonthService payrollMonthService;

    /**
     * 월급여 임시저장 / 확정 / 지급완료
     * 프론트에서 status(DRAFT/CONFIRMED/PAID)를 넘겨주면,
     * 동일한 엔드포인트로 모두 처리.
     */
    @PostMapping("/month")
    public ResponseEntity<?> savePayrollMonth(@RequestBody PayrollMonthRequest request) {

        log.info("Save payroll month request: {}", request);

        // 간단 검증 (필수값 체크 등)
        if (request.getMemberId() == null ||
                request.getPayYear() == null ||
                request.getPayMonth() == null) {
            return ResponseEntity.badRequest().body("memberId, payYear, payMonth는 필수입니다.");
        }

        PayrollMonth saved = payrollMonthService.saveMonth(request);

        // 필요한 정보만 내려 주고, 다른 화면에서 활용하려면 DTO를 따로 만드는 것도 좋습니다.
        return ResponseEntity.ok().body(saved.getId());
    }


    @GetMapping("/month/{memberId}/{year}/{month}")
    public ResponseEntity<?> getPayrollMonth(
            @PathVariable("memberId") Long memberId,
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month
    ) {
        return payrollMonthService
                .getMonth(memberId, year, month)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/member/{memberId}")
    public List<MemberMonthPayrollResponse> getPayrollsOfMember(
            @PathVariable Long memberId,
            @RequestParam(name = "status", required = false) PayrollStatus status
    ) {
        log.info("Get payrolls of member id: {}", memberId);

        return payrollMonthService.getPayrollsForMember(memberId, status);
    }
}
