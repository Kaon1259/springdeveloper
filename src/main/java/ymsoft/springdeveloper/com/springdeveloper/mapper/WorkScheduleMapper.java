package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WorkScheduleMapper {
    List<WorkSchedule> findByMemberIdAndWorkDate(@Param("memberId") Long memberId, @Param("workDate") LocalDate workDate);
    List<WorkSchedule> findByMemberIdInAndWorkDate(@Param("memberIds") List<Long> memberIds, @Param("workDate") LocalDate workDate);
    List<WorkSchedule> findByMemberIdAndWorkDateBetweenOrderByWorkDateAscStartAsc(@Param("memberId") Long memberId, @Param("start") LocalDate start, @Param("end") LocalDate end);
    long deleteByMemberIdAndWorkDate(@Param("memberId") Long memberId, @Param("workDate") LocalDate workDate);
    List<WorkSchedule> findByWorkDateWithMember(@Param("date") LocalDate date);
    void insert(WorkSchedule workSchedule);
    void deleteAll(@Param("ids") List<Long> ids);
    boolean existsByMemberId(@Param("memberId") Long memberId);
}
