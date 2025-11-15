package ymsoft.springdeveloper.com.springdeveloper.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.PayrollMonth;
import ymsoft.springdeveloper.com.springdeveloper.enums.PayrollStatus;
import ymsoft.springdeveloper.com.springdeveloper.repository.PayrollMonthRepository;
import ymsoft.springdeveloper.com.springdeveloper.repository.memberRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayrollMonthService {

    private final PayrollMonthRepository payrollMonthRepository;

    @Autowired
    private memberRepository memberRepository;

    public PayrollMonthService(PayrollMonthRepository payrollMonthRepository) {
        this.payrollMonthRepository = payrollMonthRepository;
    }

    /*
    @Transactional
    public PayrollMonth saveMonth(PayrollMonthSaveRequest req) {

        // 🔹 생성(Create)
        if (req.getId() == null) {
            if (req.getMemberId() == null) {
                throw new IllegalArgumentException("memberId 는 필수입니다.");
            }

            Member member = memberRepository.findById(req.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 아르바이트를 찾을 수 없습니다. id=" + req.getMemberId()));

            PayrollMonth pm = new PayrollMonth();

            // ❗❗ 여기서 pm.setId(...) 절대 하지 않기
            pm.setMember(member);

            pm.setPayYear(req.getPayYear());
            pm.setPayMonth(req.getPayMonth());
            pm.setMonthWorkMinutes(req.getMonthWorkMinutes());
            pm.setMonthWorkPay(req.getMonthWorkPay());
            pm.setMonthJuhyuMinutes(req.getMonthJuhyuMinutes());
            pm.setMonthJuhyuPay(req.getMonthJuhyuPay());
            pm.setMonthTotalPay(req.getMonthTotalPay());
            pm.setHourlyWage(req.getHourlyWage());

            pm.setStatus(PayrollStatus.DRAFT);     // 예시
            pm.setConfirmedAt(null);
            pm.setPaidAt(null);

            return payrollMonthRepository.save(pm);   // 🔹 여기서는 persist → INSERT
        }

        // 🔹 수정(Update)
        PayrollMonth pm = payrollMonthRepository.findById(req.getId())
                .orElseThrow(() -> new IllegalArgumentException("수정할 PayrollMonth 를 찾을 수 없습니다. id=" + req.getId()));

        // (보통 member 는 수정 안 한다고 가정)
        pm.setPayYear(req.getPayYear());
        pm.setPayMonth(req.getPayMonth());
        pm.setMonthWorkMinutes(req.getMonthWorkMinutes());
        pm.setMonthWorkPay(req.getMonthWorkPay());
        pm.setMonthJuhyuMinutes(req.getMonthJuhyuMinutes());
        pm.setMonthJuhyuPay(req.getMonthJuhyuPay());
        pm.setMonthTotalPay(req.getMonthTotalPay());
        pm.setHourlyWage(req.getHourlyWage());
        // confirmedAt, paidAt, status 등은 비즈니스 룰에 맞게

        // pm 은 이미 영속 상태라 save(pm) 호출 안 해도 flush 시 UPDATE 됨
        return pm;
    }
}
*/

    @Transactional
    public PayrollMonth saveMonth(PayrollMonthRequest req) {

        log.info("Saving month: {}", req.getMemberId());

        PayrollMonth entity = payrollMonthRepository
                .findByMemberIdAndPayYearAndPayMonth(
                        req.getMemberId(),
                        req.getPayYear(),
                        req.getPayMonth()
                )
                .orElseGet(PayrollMonth::new);

        log.info("Saving month: {}", entity);

        // 신규면 기본키/키 필드 세팅
        if (entity.getId() == null) {
            Member member = memberRepository.findById(req.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 ID의 아르바이트를 찾을 수 없습니다. id=" + req.getMemberId()));
            entity.setMember(member);
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

