package ymsoft.springdeveloper.com.springdeveloper.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ymsoft.springdeveloper.com.springdeveloper.dto.DayWorkDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeekWorkResponse;
import ymsoft.springdeveloper.com.springdeveloper.dto.WorkSegmentDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.repository.ActualWorkScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActualWorkScheduleService {

    private final ActualWorkScheduleRepository repository;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<WorkSegmentDto>> SEGMENT_LIST_TYPE =
            new TypeReference<>() {};

    @Transactional(readOnly = true)
    public Optional<ActualWorkSchedule> findByMemberAndDate(Long memberId, LocalDate workDate) {
        return repository.findByMemberIdAndWorkDate(memberId, workDate);
    }

    @Transactional(readOnly = true)
    public WeekWorkResponse getWeek(Long memberId, LocalDate start, LocalDate end) {
        List<ActualWorkSchedule> rows =
                repository.findByMemberIdAndWorkDateBetweenOrderByWorkDateAsc(memberId, start, end);

        List<DayWorkDto> days = new ArrayList<>(rows.size());
        for (ActualWorkSchedule row : rows) {
            List<WorkSegmentDto> segments = parseSegments(row.getSegmentsJson());
            int minutes = (row.getTotalMinutes() != null)
                    ? row.getTotalMinutes()
                    : calcMinutes(segments);

            days.add(new DayWorkDto(
                    row.getWorkDate().toString(),
                    segments,
                    minutes
            ));
        }
        return new WeekWorkResponse(String.valueOf(memberId), days);
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


    private List<WorkSegmentDto> parseSegments(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, SEGMENT_LIST_TYPE);
        } catch (Exception e) {
            // 파싱 실패 시 안전하게 빈 목록 리턴 (로그만 남기고 진행)
            return List.of();
        }
    }

    private int calcMinutes(List<WorkSegmentDto> segments) {
        int sum = 0;
        for (WorkSegmentDto s : segments) {
            sum += diffMinutes(s.start(), s.end());
        }
        return sum;
    }

    private int diffMinutes(String start, String end) {
        LocalTime s = LocalTime.parse(start); // "HH:mm"
        LocalTime t = LocalTime.parse(end);
        return (int) Duration.between(s, t).toMinutes();
    }
}
