package ymsoft.springdeveloper.com.springdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.ScheduleDayUpdateRequest;
import ymsoft.springdeveloper.com.springdeveloper.dto.ScheduleDayUpdateResponse;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeekDay;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeeklyTemplateUpdateRequest;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.repository.ScheduleItemRepository;
import ymsoft.springdeveloper.com.springdeveloper.repository.memberRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
public class ScheduleItemService {
    @Autowired
    ScheduleItemRepository scheduleItemRepository;

    @Autowired
    memberRepository memberRepository;

    @Transactional
    public void updateDailyTemplate(WeeklyTemplateUpdateRequest req) {
        Member member = memberRepository.findById(req.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + req.getMemberId()));

        ScheduleItem.WeekDay day = ScheduleItem.WeekDay.valueOf(req.getDay());  // "MON" → DayCode.MON

        // 기존 이 멤버 + 해당 요일 템플릿 싹 지우고
        scheduleItemRepository.deleteByMemberAndDay(member, day);

        if (req.getSegments() == null || req.getSegments().isEmpty()) {
            // 휴무인 경우 → row 없이 종료
            return;
        }

        // 새로 다 넣기
        for (WeeklyTemplateUpdateRequest.Segment seg : req.getSegments()) {
            // "HH:mm" → LocalTime
            // end/start 에 ":00" 붙여야 하면 아래처럼
            LocalTime start = LocalTime.parse(seg.getStart().length() == 5
                    ? seg.getStart() + ":00" : seg.getStart());
            LocalTime end   = LocalTime.parse(seg.getEnd().length() == 5
                    ? seg.getEnd() + ":00" : seg.getEnd());

            ScheduleItem entity = ScheduleItem.builder()
                    .member(member)
                    .day(day)
                    .start(start)
                    .end(end)
                    .build();

            scheduleItemRepository.save(entity);
        }
    }
}
