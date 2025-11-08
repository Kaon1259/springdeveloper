package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;

import java.time.LocalDate;
import java.util.List;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    /** 특정 근로자의 특정 날짜 모든 일정 */
    List<WorkSchedule> findByMember_IdAndWorkDate(Long memberId, LocalDate workDate);

    List<WorkSchedule> findByMemberIdAndWorkDateBetweenOrderByWorkDateAscStartAsc(
            Long memberId, LocalDate start, LocalDate end
    );

    List<WorkSchedule> findByMemberIdAndWorkDate(Long memberId, LocalDate workDate);

    void deleteByMemberIdAndWorkDate(Long memberId, LocalDate workDate);

    @Query("""
        select ws
        from WorkSchedule ws
        join fetch ws.member m
        where ws.workDate = :date
        """)
    List<WorkSchedule> findByWorkDateWithMember(@Param("date") LocalDate date);
}

