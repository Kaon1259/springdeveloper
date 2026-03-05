package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeeklyTemplateUpdateRequest;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;
import ymsoft.springdeveloper.com.springdeveloper.mapper.MemberMapper;
import ymsoft.springdeveloper.com.springdeveloper.mapper.ScheduleItemMapper;

import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleItemService {

    private final ScheduleItemMapper scheduleItemMapper;
    private final MemberMapper memberMapper;

    @Transactional
    public void updateDailyTemplate(WeeklyTemplateUpdateRequest req) {
        Member member = memberMapper.findById(req.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + req.getMemberId()));

        ScheduleItem.WeekDay day = ScheduleItem.WeekDay.valueOf(req.getDay());
        scheduleItemMapper.deleteByMemberIdAndDay(member.getId(), day.name());

        if (req.getSegments() == null || req.getSegments().isEmpty()) return;

        for (WeeklyTemplateUpdateRequest.Segment seg : req.getSegments()) {
            LocalTime start = LocalTime.parse(seg.getStart().length() == 5 ? seg.getStart() + ":00" : seg.getStart());
            LocalTime end   = LocalTime.parse(seg.getEnd().length() == 5   ? seg.getEnd()   + ":00" : seg.getEnd());
            ScheduleItem entity = ScheduleItem.builder()
                    .memberId(member.getId())
                    .day(day)
                    .start(start)
                    .end(end)
                    .build();
            scheduleItemMapper.insert(entity);
        }
    }
}
