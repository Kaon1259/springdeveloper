package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;
import ymsoft.springdeveloper.com.springdeveloper.mapper.MemberMapper;
import ymsoft.springdeveloper.com.springdeveloper.mapper.PayrollMonthMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollMonthService {

    private final PayrollMonthMapper payrollMonthMapper;
    private final MemberMapper memberMapper;

    @Transactional
    public PayrollMonth saveMonth(PayrollMonthRequest req) {
        log.info("Saving payroll for memberId: {}", req.getMemberId());

        Optional<PayrollMonth> existing = payrollMonthMapper
                .findByMemberIdAndPayYearAndPayMonth(req.getMemberId(), req.getPayYear(), req.getPayMonth());

        PayrollMonth entity;
        if (existing.isPresent()) {
            entity = existing.get();
        } else {
            Member member = memberMapper.findById(req.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "해당 ID의 아르바이트를 찾을 수 없습니다. id=" + req.getMemberId()));
            entity = new PayrollMonth();
            entity.setMemberId(member.getId());
            entity.setPayYear(req.getPayYear());
            entity.setPayMonth(req.getPayMonth());
        }

        entity.setHourlyWage(req.getHourlyWage());
        entity.setMonthWorkMinutes(req.getMonthWorkMinutes());
        entity.setMonthJuhyuMinutes(req.getMonthJuhyuMinutes());
        entity.setMonthWorkPay(req.getMonthWorkPay());
        entity.setMonthJuhyuPay(req.getMonthJuhyuPay());
        entity.setMonthTotalPay(req.getMonthTotalPay());

        PayrollStatus newStatus = toStatus(req.getStatus());
        entity.setStatus(newStatus);

        LocalDateTime now = LocalDateTime.now();
        switch (newStatus) {
            case DRAFT -> { entity.setConfirmedAt(null); entity.setPaidAt(null); }
            case CONFIRMED -> { if (entity.getConfirmedAt() == null) entity.setConfirmedAt(now); }
            case PAID -> { if (entity.getConfirmedAt() == null) entity.setConfirmedAt(now); entity.setPaidAt(now); }
        }

        if (entity.getId() == null) {
            payrollMonthMapper.insert(entity);
        } else {
            payrollMonthMapper.update(entity);
        }
        return entity;
    }

    private PayrollStatus toStatus(String status) {
        if (status == null) return PayrollStatus.DRAFT;
        try { return PayrollStatus.valueOf(status.toUpperCase()); }
        catch (IllegalArgumentException e) { return PayrollStatus.DRAFT; }
    }

    @Transactional(readOnly = true)
    public Optional<PayrollMonthResponse> getMonth(Long memberId, Integer year, Integer month) {
        return payrollMonthMapper.findByMemberIdAndPayYearAndPayMonth(memberId, year, month)
                .map(PayrollMonthResponse::fromEntity);
    }

    public List<MemberMonthPayrollResponse> getPayrollsForMember(Long memberId, PayrollStatus status) {
        List<PayrollMonth> entities = (status != null)
                ? payrollMonthMapper.findByMemberIdAndStatusOrderByPayYearDescPayMonthDesc(memberId, status.name())
                : payrollMonthMapper.findByMemberIdOrderByPayYearDescPayMonthDesc(memberId);
        return entities.stream().map(MemberMonthPayrollResponse::from).toList();
    }

    public List<MonthlyPayrollDto> getMonthlyPayroll(int year, int month) {
        return payrollMonthMapper.findByPayYearAndPayMonth(year, month).stream()
                .map(p -> MonthlyPayrollDto.builder()
                        .memberId(p.getMember().getId())
                        .memberName(p.getMember().getName())
                        .memberPhone(p.getMember().getPhone())
                        .workMinutes(p.getMonthWorkMinutes())
                        .juhyuMinutes(p.getMonthJuhyuMinutes())
                        .hourlyWage(p.getHourlyWage())
                        .workPay(p.getMonthWorkPay().intValue())
                        .juhyuPay(p.getMonthJuhyuPay().intValue())
                        .totalPay(p.getMonthWorkPay().intValue() + p.getMonthJuhyuPay().intValue())
                        .status(p.getStatus().name())
                        .createdAt(formatDate(p.getCreatedAt()))
                        .confirmedAt(formatDate(p.getConfirmedAt()))
                        .paidAt(formatDate(p.getPaidAt()))
                        .build())
                .collect(Collectors.toList());
    }

    public List<YearlyPayrollDto> getYearlyPayroll(int year) {
        return payrollMonthMapper.findYearlySummaryByYear(year);
    }

    private String formatDate(LocalDateTime t) {
        return t != null ? t.toString() : null;
    }
}
