package ymsoft.springdeveloper.com.springdeveloper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.DayWorkDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.UpdateWorkScheduleRequest;
import ymsoft.springdeveloper.com.springdeveloper.dto.WeekWorkResponse;
import ymsoft.springdeveloper.com.springdeveloper.dto.WorkSegmentDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.mapper.ActualWorkScheduleMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ActualWorkScheduleService {

    private final ActualWorkScheduleMapper mapper;
    private final ObjectMapper objectMapper;

    private static final TypeReference<List<WorkSegmentDto>> SEGMENT_LIST_TYPE = new TypeReference<>() {};

    @Transactional(readOnly = true)
    public Optional<ActualWorkSchedule> findByMemberAndDate(Long memberId, LocalDate workDate) {
        return mapper.findByMemberIdAndWorkDate(memberId, workDate);
    }

    @Transactional(readOnly = true)
    public WeekWorkResponse getWeek(Long memberId, LocalDate start, LocalDate end) {
        List<ActualWorkSchedule> rows =
                mapper.findByMemberIdAndWorkDateBetweenOrderByWorkDateAsc(memberId, start, end);
        List<DayWorkDto> days = new ArrayList<>(rows.size());
        for (ActualWorkSchedule row : rows) {
            List<WorkSegmentDto> segments = parseSegments(row.getSegmentsJson());
            int minutes = (row.getTotalMinutes() != null) ? row.getTotalMinutes() : calcMinutes(segments);
            days.add(new DayWorkDto(row.getWorkDate().toString(), segments, minutes));
        }
        return new WeekWorkResponse(String.valueOf(memberId), days);
    }

    @Transactional
    public ActualWorkSchedule save(ActualWorkSchedule schedule) {
        Optional<ActualWorkSchedule> existing =
                mapper.findByMemberIdAndWorkDate(schedule.getMemberId(), schedule.getWorkDate());
        if (existing.isPresent()) {
            ActualWorkSchedule entity = existing.get();
            entity.setSegmentsJson(schedule.getSegmentsJson());
            entity.setTotalMinutes(schedule.getTotalMinutes());
            mapper.update(entity);
            return entity;
        } else {
            mapper.insert(schedule);
            return schedule;
        }
    }

    @Transactional(readOnly = true)
    public List<ActualWorkSchedule> getTodaySchedules() {
        return mapper.findByWorkDate(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Optional<ActualWorkSchedule> getByMemberAndDate(Long memberId, LocalDate date) {
        return mapper.findByMemberIdAndWorkDate(memberId, date);
    }

    @Transactional(readOnly = true)
    public List<ActualWorkSchedule> getRecent(Long memberId) {
        return mapper.findByMemberIdOrderByWorkDateDesc(memberId);
    }

    @Transactional
    public void updateActualWorkSchedule(UpdateWorkScheduleRequest request) {
        Long memberId = request.getMemberId();
        LocalDate workDate = request.getDate();
        List<UpdateWorkScheduleRequest.SegmentDto> segments = request.getSegments();
        log.info("updateActualWorkSchedule: {}", segments.toString());

        Integer totalMinutes = 0;
        if (segments != null && !segments.isEmpty()) {
            List<SegmentWithTime> timeSegments = segments.stream()
                    .map(s -> {
                        LocalTime start = LocalTime.parse(s.getStart());
                        LocalTime end = LocalTime.parse(s.getEnd());
                        if (!end.isAfter(start)) {
                            throw new IllegalArgumentException("end는 start보다 이후여야 합니다: " + s.getStart() + " ~ " + s.getEnd());
                        }
                        return new SegmentWithTime(start, end);
                    })
                    .sorted(Comparator.comparing(SegmentWithTime::start)).toList();

            for (int i = 1; i < timeSegments.size(); i++) {
                SegmentWithTime prev = timeSegments.get(i - 1);
                SegmentWithTime cur = timeSegments.get(i);
                if (!cur.start().isAfter(prev.end())) {
                    throw new IllegalArgumentException("근무 구간이 서로 겹칩니다: " +
                            prev.start() + "~" + prev.end() + " / " + cur.start() + "~" + cur.end());
                }
            }
            for (SegmentWithTime s : timeSegments) {
                totalMinutes += (int) Duration.between(s.start(), s.end()).toMinutes();
            }
        }

        String segmentsJson;
        try {
            segmentsJson = objectMapper.writeValueAsString(segments == null ? List.of() : segments);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("segments 직렬화 실패", e);
        }

        Optional<ActualWorkSchedule> optional = mapper.findByMemberIdAndWorkDate(memberId, workDate);
        final Integer totalMinutesFinal = totalMinutes;

        if (optional.isPresent()) {
            ActualWorkSchedule entity = optional.get();
            entity.setSegmentsJson(segmentsJson);
            entity.setTotalMinutes(totalMinutesFinal);
            mapper.update(entity);
        } else {
            ActualWorkSchedule entity = ActualWorkSchedule.builder()
                    .memberId(memberId).workDate(workDate)
                    .segmentsJson(segmentsJson).totalMinutes(totalMinutesFinal).build();
            mapper.insert(entity);
        }
    }

    private record SegmentWithTime(LocalTime start, LocalTime end) {}

    private List<WorkSegmentDto> parseSegments(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();
            return objectMapper.readValue(json, SEGMENT_LIST_TYPE);
        } catch (Exception e) {
            return List.of();
        }
    }

    private int calcMinutes(List<WorkSegmentDto> segments) {
        int sum = 0;
        for (WorkSegmentDto s : segments) { sum += diffMinutes(s.start(), s.end()); }
        return sum;
    }

    private int diffMinutes(String start, String end) {
        LocalTime s = LocalTime.parse(start);
        LocalTime t = LocalTime.parse(end);
        return (int) Duration.between(s, t).toMinutes();
    }
}
