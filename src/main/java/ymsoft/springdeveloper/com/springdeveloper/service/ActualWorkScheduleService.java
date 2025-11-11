package ymsoft.springdeveloper.com.springdeveloper.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ymsoft.springdeveloper.com.springdeveloper.dto.DayWorkDto;
import ymsoft.springdeveloper.com.springdeveloper.dto.UpdateWorkScheduleRequest;
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
import java.util.*;

@Slf4j
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

    @Transactional
    public void updateActualWorkSchedule(UpdateWorkScheduleRequest request) {

        Long memberId = request.getMemberId();
        LocalDate workDate = request.getDate();
        List<UpdateWorkScheduleRequest.SegmentDto> segments = request.getSegments();
        log.info("updateActualWorkSchedule: {}", segments.toString());

        // 1) segments 검증 및 totalMinutes 계산
        Integer totalMinutes = 0;
        if (segments != null && !segments.isEmpty()) {
            // 시작/끝 검증 + LocalTime 변환 리스트 생성
            List<SegmentWithTime> timeSegments = segments.stream()
                    .map(s -> {
                        LocalTime start = LocalTime.parse(s.getStart()); // "HH:mm"
                        LocalTime end   = LocalTime.parse(s.getEnd());
                        if (!end.isAfter(start)) {
                            throw new IllegalArgumentException(
                                    "end는 start보다 이후여야 합니다: " +
                                            s.getStart() + " ~ " + s.getEnd()
                            );
                        }
                        return new SegmentWithTime(start, end);
                    })
                    .sorted(Comparator.comparing(SegmentWithTime::start))
                    .toList();

            // 겹치는지 검증
            for (int i = 1; i < timeSegments.size(); i++) {
                SegmentWithTime prev = timeSegments.get(i - 1);
                SegmentWithTime cur  = timeSegments.get(i);
                if (!cur.start().isAfter(prev.end())) {
                    throw new IllegalArgumentException(
                            "근무 구간이 서로 겹칩니다: " +
                                    prev.start() + "~" + prev.end() + " / " +
                                    cur.start() + "~" + cur.end()
                    );
                }
            }

            // 총 근무 시간 분(minute) 계산
            for (SegmentWithTime s : timeSegments) {
                totalMinutes += (int) Duration.between(s.start(), s.end()).toMinutes();
            }
        } else {
            // segments 비어 있으면 휴무로 간주
            totalMinutes = 0;
        }

        // 2) segments를 JSON 문자열로 직렬화
        String segmentsJson;
        try {
            // 프론트가 보내준 구조 그대로 저장
            segmentsJson = objectMapper.writeValueAsString(
                    segments == null ? List.of() : segments
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("segments 직렬화 실패", e);
        }

        // 3) memberId + workDate 기준으로 upsert
        Optional<ActualWorkSchedule> optional = repository
                .findByMemberIdAndWorkDate(memberId, workDate);

        final Integer totalMinutesFinal = totalMinutes;

        ActualWorkSchedule entity = optional
                .map(existing -> {
                    existing.setSegmentsJson(segmentsJson);
                    existing.setTotalMinutes(totalMinutesFinal);
                    return existing;
                })
                .orElseGet(() -> ActualWorkSchedule.builder()
                        .memberId(memberId)
                        .workDate(workDate)
                        .segmentsJson(segmentsJson)
                        .totalMinutes(totalMinutesFinal)
                        .build());

        repository.save(entity);
    }

    /**
     * 검증과 계산용 내부 타입
     */
    private record SegmentWithTime(LocalTime start, LocalTime end) {}


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
