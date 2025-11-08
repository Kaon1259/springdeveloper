package ymsoft.springdeveloper.com.springdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.ScheduleGenerateRequest;
import ymsoft.springdeveloper.com.springdeveloper.dto.ScheduleGenerateResponse;
import ymsoft.springdeveloper.com.springdeveloper.dto.ScheduleRangeResponse;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.repository.memberRepository;
import ymsoft.springdeveloper.com.springdeveloper.repository.WorkScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final memberRepository memberRepository;
    private final WorkScheduleRepository workScheduleRepository;

    @Transactional
    public ScheduleGenerateResponse generateSchedules(ScheduleGenerateRequest req) {
        // 1) 멤버 확인
        Member member = memberRepository.findById(req.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근로자(memberId=" + req.getMemberId() + ") 입니다."));

        int totalRequested = req.getItems().size();
        int created = 0;
        int skipped = 0;
        int overwritten = 0;

        List<Long> createdIds = new ArrayList<>();
        List<ScheduleGenerateResponse.Conflict> conflicts = new ArrayList<>();

        // 2) 날짜 범위 검증
        if (req.getFrom().isAfter(req.getTo())) {
            throw new IllegalArgumentException("from 은 to 보다 이후일 수 없습니다.");
        }

        for (ScheduleGenerateRequest.Item item : req.getItems()) {

            // 2-1) 요청 항목 기본 검증
            LocalDate date = item.getDate();
            LocalTime start = item.getStart();
            LocalTime end = item.getEnd();

            // 범위 밖의 날짜는 스킵
            if (date.isBefore(req.getFrom()) || date.isAfter(req.getTo())) {
                skipped++;
                conflicts.add(ScheduleGenerateResponse.Conflict.builder()
                        .date(String.valueOf(date))
                        .start(start != null ? start.toString() : null)
                        .end(end != null ? end.toString() : null)
                        .existingId(null)
                        .reason("OUT_OF_RANGE")
                        .build());
                continue;
            }

            // 시각/10분단위/역전 검증
            String invalidReason = validateTimes(start, end);
            if (invalidReason != null) {
                skipped++;
                conflicts.add(ScheduleGenerateResponse.Conflict.builder()
                        .date(date.toString())
                        .start(start != null ? start.toString() : null)
                        .end(end != null ? end.toString() : null)
                        .existingId(null)
                        .reason(invalidReason) // INVALID_TIME / INVALID_10MIN / REVERSED
                        .build());
                continue;
            }

            // 3) 같은 날짜 기존 일정 조회 후 겹침 판단
            List<WorkSchedule> existing = workScheduleRepository.findByMember_IdAndWorkDate(member.getId(), date);
            List<WorkSchedule> overlapped = existing.stream()
                    .filter(ws -> isOverlap(start, end, ws.getStart(), ws.getEnd()))
                    .toList();

            if (!overlapped.isEmpty()) {
                if (req.isOverwrite()) {
                    // 덮어쓰기: 겹치는 일정 삭제 후 새로 저장
                    overlapped.forEach(workScheduleRepository::delete);
                    overwritten += overlapped.size();

                    WorkSchedule saved = saveOne(member, date, start, end, item.getNote());
                    created++;
                    createdIds.add(saved.getId());
                } else {
                    // 덮어쓰기 아님: 스킵 + 충돌 등록(첫 건만 existingId 노출)
                    skipped++;
                    Long firstId = overlapped.get(0).getId();
                    conflicts.add(ScheduleGenerateResponse.Conflict.builder()
                            .date(date.toString())
                            .start(start.toString())
                            .end(end.toString())
                            .existingId(firstId)
                            .reason("DUPLICATED")
                            .build());
                }
            } else {
                // 4) 겹치지 않으면 저장
                WorkSchedule saved = saveOne(member, date, start, end, item.getNote());
                created++;
                createdIds.add(saved.getId());
            }
        }

        return ScheduleGenerateResponse.builder()
                .totalRequested(totalRequested)
                .created(created)
                .skipped(skipped)
                .overwritten(overwritten)
                .createdIds(createdIds)
                .conflicts(conflicts)
                .build();
    }

    private WorkSchedule saveOne(Member member, LocalDate date, LocalTime start, LocalTime end, String note) {
        WorkSchedule ws = WorkSchedule.builder()
                .member(member)
                .workDate(date)
                .start(start)
                .end(end)
                .source(WorkSchedule.SourceType.GENERATED)
                .note(note)
                .build();
        return workScheduleRepository.save(ws);
    }
    public ScheduleRangeResponse getWorkRange(Long memberId, LocalDate start, LocalDate end) {
        validateParams(memberId, start, end);

        // (선택) 멤버 존재 여부 체크
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 근로자입니다. memberId=" + memberId);
        }

        List<WorkSchedule> schedules =
                workScheduleRepository.findByMemberIdAndWorkDateBetweenOrderByWorkDateAscStartAsc(
                        memberId, start, end
                );

        // workDate 기준으로 그룹핑(정렬 유지)
        Map<LocalDate, List<WorkSchedule>> byDate = schedules.stream()
                .collect(Collectors.groupingBy(
                        WorkSchedule::getWorkDate,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        ScheduleRangeResponse resp = new ScheduleRangeResponse();

        byDate.forEach((date, list) -> {
            // 해당 날짜의 구간 목록
            List<ScheduleRangeResponse.Seg> segs = list.stream()
                    .map(ws -> ScheduleRangeResponse.Seg.builder()
                            .start(ws.getStart())
                            .end(ws.getEnd())
                            .note(ws.getNote())
                            .build())
                    .collect(Collectors.toList());

            int minutes = list.stream()
                    .mapToInt(ws -> diffMinutes(ws.getStart(), ws.getEnd()))
                    .filter(m -> m > 0)
                    .sum();

            resp.getDays().add(ScheduleRangeResponse.Day.builder()
                    .date(date)
                    .segments(segs)
                    .minutes(minutes)
                    .build());
        });

        return resp;
    }

    private void validateParams(Long memberId, LocalDate start, LocalDate end) {
        if (memberId == null) throw new IllegalArgumentException("memberId는 필수입니다.");
        if (start == null) throw new IllegalArgumentException("start는 필수입니다.");
        if (end == null) throw new IllegalArgumentException("end는 필수입니다.");
        if (end.isBefore(start)) throw new IllegalArgumentException("end는 start보다 빠를 수 없습니다.");
    }

    private int diffMinutes(LocalTime s, LocalTime e) {
        if (s == null || e == null) return 0;
        return (e.getHour() * 60 + e.getMinute()) - (s.getHour() * 60 + s.getMinute());
    }


    /** 겹침 판단: [aStart, aEnd) 와 [bStart, bEnd) 가 교차하는가 */
    private boolean isOverlap(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    /** 시간 검증: null / 역전 / 10분 단위 검사 */
    private String validateTimes(LocalTime start, LocalTime end) {
        if (start == null || end == null) return "INVALID_TIME";
        if (!isTenMinuteAligned(start) || !isTenMinuteAligned(end)) return "INVALID_10MIN";
        if (!start.isBefore(end)) return "REVERSED";
        return null;
    }

    private boolean isTenMinuteAligned(LocalTime t) {
        return t.getMinute() % 10 == 0;
    }
}

