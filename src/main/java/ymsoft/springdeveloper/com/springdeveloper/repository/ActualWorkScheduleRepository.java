package ymsoft.springdeveloper.com.springdeveloper.repository;

import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActualWorkScheduleRepository extends JpaRepository<ActualWorkSchedule, Long> {

    /** 특정 회원의 특정 일자 근무 기록 조회 */
    Optional<ActualWorkSchedule> findByMemberIdAndWorkDate(Long memberId, LocalDate workDate);

    /** 특정 회원의 최근 근무 내역 조회 (최근 10일 등) */
    List<ActualWorkSchedule> findByMemberIdOrderByWorkDateDesc(Long memberId);

    /** 특정 일자의 모든 근무자 목록 조회 */
    List<ActualWorkSchedule> findByWorkDate(LocalDate workDate);

    /** 특정 날짜 범위의 근무 내역 */
    List<ActualWorkSchedule> findByWorkDateBetween(LocalDate startDate, LocalDate endDate);

    List<ActualWorkSchedule> findByMemberIdAndWorkDateBetweenOrderByWorkDateAsc(
            Long memberId, LocalDate start, LocalDate end
    );
}

