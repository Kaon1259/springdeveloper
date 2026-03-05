package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface ActualWorkScheduleMapper {
    Optional<ActualWorkSchedule> findByMemberIdAndWorkDate(@Param("memberId") Long memberId, @Param("workDate") LocalDate workDate);
    List<ActualWorkSchedule> findByMemberIdOrderByWorkDateDesc(@Param("memberId") Long memberId);
    List<ActualWorkSchedule> findByWorkDate(@Param("workDate") LocalDate workDate);
    List<ActualWorkSchedule> findByMemberIdAndWorkDateBetweenOrderByWorkDateAsc(@Param("memberId") Long memberId, @Param("start") LocalDate start, @Param("end") LocalDate end);
    void insert(ActualWorkSchedule schedule);
    void update(ActualWorkSchedule schedule);
}
