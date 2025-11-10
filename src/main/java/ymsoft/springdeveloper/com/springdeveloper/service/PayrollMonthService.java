package ymsoft.springdeveloper.com.springdeveloper.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;
import ymsoft.springdeveloper.com.springdeveloper.repository.PayrollMonthRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PayrollMonthService {

    private final PayrollMonthRepository payrollMonthRepository;

    public PayrollMonthService(PayrollMonthRepository payrollMonthRepository) {
        this.payrollMonthRepository = payrollMonthRepository;
    }

    @Transactional
    public PayrollMonth saveMonth(PayrollMonthRequest req) {

        PayrollMonth entity = payrollMonthRepository
                .findByMemberIdAndPayYearAndPayMonth(
                        req.getMemberId(),
                        req.getPayYear(),
                        req.getPayMonth()
                )
                .orElseGet(PayrollMonth::new);

        // 신규면 기본키/키 필드 세팅
        if (entity.getId() == null) {
        //    entity.setMemberId(req.getMemberId());
            entity.setMember(entity.getMember());
            entity.setPayYear(req.getPayYear());
            entity.setPayMonth(req.getPayMonth());
        }

        // 금액/시간 정보 업데이트
        entity.setHourlyWage(req.getHourlyWage());
        entity.setMonthWorkMinutes(req.getMonthWorkMinutes());
        entity.setMonthJuhyuMinutes(req.getMonthJuhyuMinutes());
        entity.setMonthWorkPay(req.getMonthWorkPay());
        entity.setMonthJuhyuPay(req.getMonthJuhyuPay());
        entity.setMonthTotalPay(req.getMonthTotalPay());

        // 상태 변경
        PayrollStatus newStatus = toStatus(req.getStatus());
        PayrollStatus oldStatus = entity.getStatus();

        // 기본값(최초 저장시)
        if (oldStatus == null) {
            oldStatus = PayrollStatus.DRAFT;
        }

        entity.setStatus(newStatus);

        LocalDateTime now = LocalDateTime.now();

        // 상태에 따른 confirmed_at / paid_at 처리
        switch (newStatus) {
            case DRAFT -> {
                // 임시저장 상태로 되돌릴 경우, 확정/지급일을 초기화할지 여부는 정책에 따라
                entity.setConfirmedAt(null);
                entity.setPaidAt(null);
            }
            case CONFIRMED -> {
                // 이미 확정되어 있지 않았다면 now로 세팅
                if (entity.getConfirmedAt() == null) {
                    entity.setConfirmedAt(now);
                }
                // paid_at은 그대로 둠
            }
            case PAID -> {
                // 확정 시간이 없으면 같이 세팅
                if (entity.getConfirmedAt() == null) {
                    entity.setConfirmedAt(now);
                }
                entity.setPaidAt(now);
            }
        }

        return payrollMonthRepository.save(entity);
    }

    private PayrollStatus toStatus(String status) {
        if (status == null) return PayrollStatus.DRAFT;
        try {
            return PayrollStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 이상한 값 들어오면 그냥 DRAFT로 처리
            return PayrollStatus.DRAFT;
        }
    }

    // ✅ 추가: 특정 회원, 특정 연/월의 월급여 조회
    @Transactional(readOnly = true)
    public Optional<PayrollMonthResponse> getMonth(Long memberId, Integer year, Integer month) {
        return payrollMonthRepository
                .findByMemberIdAndPayYearAndPayMonth(memberId, year, month)
                .map(PayrollMonthResponse::fromEntity);
    }


    public List<MemberMonthPayrollResponse> getPayrollsForMember(Long memberId, PayrollStatus status) {

        List<PayrollMonth> entities;

        if (status != null) {
            entities = payrollMonthRepository
                    .findByMemberIdAndStatusOrderByPayYearDescPayMonthDesc(memberId, status);
        } else {
            entities = payrollMonthRepository
                    .findByMemberIdOrderByPayYearDescPayMonthDesc(memberId);
        }

        return entities.stream()
                .map(MemberMonthPayrollResponse::from)
                .toList();
    }


    /**
     * 특정 연월 기준으로 근무자별 급여 요약 목록 조회
     */
    public List<MonthlyPayrollDto> getMonthlyPayroll(int year, int month) {
        List<PayrollMonth> entities = payrollMonthRepository.findByPayYearAndPayMonth(year, month);

        return entities.stream()
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
                        .build()
                )
                .collect(Collectors.toList());
    }

    /**
     * 연도별 근무자 급여 합계 조회
     */
    public List<YearlyPayrollDto> getYearlyPayroll(int year) {
        // 필요하면 year 유효성 체크 (예: 과거/미래 제한) 추가 가능
        return payrollMonthRepository.findYearlySummaryByYear(year);
    }

    private String formatDate(LocalDateTime t) {
        if (t == null) return null;
        return t.toString(); // ISO 문자열로 반환
    }
}

