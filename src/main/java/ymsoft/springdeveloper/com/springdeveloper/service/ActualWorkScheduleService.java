package ymsoft.springdeveloper.com.springdeveloper.service;

import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.repository.ActualWorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActualWorkScheduleService {

    private final ActualWorkScheduleRepository repository;

    @Transactional(readOnly = true)
    public Optional<ActualWorkSchedule> findByMemberAndDate(Long memberId, LocalDate workDate) {
        return repository.findByMemberIdAndWorkDate(memberId, workDate);
    }

    /** 저장 또는 업데이트 */
    @Transactional
    public ActualWorkSchedule save(ActualWorkSchedule schedule) {
        Optional<ActualWorkSchedule> existing =
                repository.findByMemberIdAndWorkDate(schedule.getMemberId(), schedule.getWorkDate());

        if (existing.isPresent()) {
            // 이미 있으면 update
            ActualWorkSchedule entity = existing.get();
            entity.setSegmentsJson(schedule.getSegmentsJson());
            entity.setTotalMinutes(schedule.getTotalMinutes());
            return repository.save(entity);
        } else {
            // 없으면 insert
            return repository.save(schedule);
        }
    }

    /** 오늘 근무자 조회 */
    @Transactional(readOnly = true)
    public List<ActualWorkSchedule> getTodaySchedules() {
        return repository.findByWorkDate(LocalDate.now());
    }

    /** 특정 회원 + 일자 근무 내역 조회 */
    @Transactional(readOnly = true)
    public Optional<ActualWorkSchedule> getByMemberAndDate(Long memberId, LocalDate date) {
        return repository.findByMemberIdAndWorkDate(memberId, date);
    }

    /** 최근 10일 내역 */
    @Transactional(readOnly = true)
    public List<ActualWorkSchedule> getRecent(Long memberId) {
        return repository.findByMemberIdOrderByWorkDateDesc(memberId);
    }
}
