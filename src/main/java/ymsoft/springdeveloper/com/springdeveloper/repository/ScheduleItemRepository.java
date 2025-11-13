package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeekDay;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {

    void deleteByMemberAndDay(Member member, ScheduleItem.WeekDay day);
}
