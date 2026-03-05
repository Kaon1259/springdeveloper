package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;

import java.util.List;

@Mapper
public interface ScheduleItemMapper {
    void deleteByMemberIdAndDay(@Param("memberId") Long memberId, @Param("day") String day);
    void insert(ScheduleItem scheduleItem);
    List<ScheduleItem> findByMemberId(@Param("memberId") Long memberId);
}
