package ymsoft.springdeveloper.com.springdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.repository.memberRepository;
import ymsoft.springdeveloper.com.springdeveloper.repository.WorkScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final memberRepository memberRepository;
    private final WorkScheduleRepository workScheduleRepository;

    private static final int SLOT_MIN = 10;
    private static final LocalTime START_BOUND = LocalTime.of(6, 0);
    private static final LocalTime END_BOUND   = LocalTime.of(22, 0); // exclusive

    public List<WorkSchedule> findByWorkDateWithMember(LocalDate date){
        return workScheduleRepository.findByWorkDateWithMember(date);
    }

    @Transactional
    public ScheduleDayUpdateResponse upsertDay(ScheduleDayUpdateRequest req) {
        final Long memberId = req.getMemberId();
        final LocalDate date = req.getDate();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId: " + memberId));

        // 1) 구간 정규화
        List<ScheduleDayUpdateRequest.Segment> normalized = normalizeAndValidate(req.getSegments());

        // 2) 해당 날짜 기존 일정 전체 삭제 + 즉시 flush
        workScheduleRepository.deleteByMemberIdAndWorkDate(memberId, date);
        workScheduleRepository.flush(); // ★ 인덱스 충돌 방지(삭제를 즉시 DB반영)

        // 3) 저장 전 동일 구간 de-dup (start|end 기준)
        Set<String> seen = new HashSet<>();
        List<WorkSchedule> toSave = new ArrayList<>();
        for (ScheduleDayUpdateRequest.Segment seg : normalized) {
            String key = seg.getStart() + "|" + seg.getEnd();
            if (!seen.add(key)) continue; // ★ 동일 구간 중복 방지

            WorkSchedule ws = WorkSchedule.builder()
                    .member(member)
                    .workDate(date)
                    .start(seg.getStart())
                    .end(seg.getEnd())
                    .source(WorkSchedule.SourceType.MANUAL)
                    .note(seg.getNote())
                    .build();
            toSave.add(ws);
        }

        // 4) 저장
        if (toSave.isEmpty()) {
            return ScheduleDayUpdateResponse.builder()
                    .memberId(memberId)
                    .date(date)
                    .count(0)
                    .minutes(0)
                    .segments(List.of())
                    .build();
        }

        List<WorkSchedule> saved = workScheduleRepository.saveAll(toSave);

        int minutes = saved.stream()
                .mapToInt(s -> diffMinutes(s.getStart(), s.getEnd()))
                .sum();

        List<ScheduleDayUpdateResponse.Segment> outSegs = saved.stream()
                .sorted(Comparator.comparing(WorkSchedule::getStart))
                .map(s -> ScheduleDayUpdateResponse.Segment.builder()
                        .start(fmt(s.getStart()))
                        .end(fmt(s.getEnd()))
                        .note(s.getNote())
                        .build())
                .toList();

        return ScheduleDayUpdateResponse.builder()
                .memberId(memberId)
                .date(date)
                .count(saved.size())
                .minutes(minutes)
                .segments(outSegs)
                .build();
    }

    // ====== 이하 보조 메서드(그대로 사용) ======

    private List<ScheduleDayUpdateRequest.Segment> normalizeAndValidate(List<ScheduleDayUpdateRequest.Segment> segments) {
        if (segments == null || segments.isEmpty()) return List.of();

        List<ScheduleDayUpdateRequest.Segment> clipped = new ArrayList<>();
        for (ScheduleDayUpdateRequest.Segment s : segments) {
            LocalTime st = s.getStart();
            LocalTime en = s.getEnd();
            if (st == null || en == null) continue;

            if (st.isBefore(START_BOUND)) st = START_BOUND;
            if (en.isAfter(END_BOUND))    en = END_BOUND;
            if (!st.isBefore(en)) continue;

            st = snapDownTo10(st);
            en = snapUpTo10(en);
            if (!st.isBefore(en)) continue;

            if (!isTenMinUnit(st) || !isTenMinUnit(en)) {
                throw new IllegalArgumentException("구간은 10분 단위여야 합니다. (" + st + "~" + en + ")");
            }

            clipped.add(ScheduleDayUpdateRequest.Segment.builder()
                    .start(st).end(en).note(s.getNote()).build());
        }
        if (clipped.isEmpty()) return List.of();

        clipped.sort(Comparator.comparing(ScheduleDayUpdateRequest.Segment::getStart));

        List<ScheduleDayUpdateRequest.Segment> merged = new ArrayList<>();
        LocalTime curS = clipped.get(0).getStart();
        LocalTime curE = clipped.get(0).getEnd();
        String curNote = clipped.get(0).getNote();

        for (int i = 1; i < clipped.size(); i++) {
            LocalTime s2 = clipped.get(i).getStart();
            LocalTime e2 = clipped.get(i).getEnd();
            String n2 = clipped.get(i).getNote();

            if (!s2.isAfter(curE)) {                 // 겹침
                if (e2.isAfter(curE)) curE = e2;
            } else if (s2.equals(curE)) {            // 인접
                curE = e2;
            } else {
                merged.add(ScheduleDayUpdateRequest.Segment.builder().start(curS).end(curE).note(curNote).build());
                curS = s2; curE = e2; curNote = n2;
            }
        }
        merged.add(ScheduleDayUpdateRequest.Segment.builder().start(curS).end(curE).note(curNote).build());

        return merged;
    }

    private static boolean isTenMinUnit(LocalTime t) {
        return t.getMinute() % SLOT_MIN == 0 && t.getSecond() == 0 && t.getNano() == 0;
    }
    private static LocalTime snapDownTo10(LocalTime t) {
        int m = t.getMinute() - (t.getMinute() % SLOT_MIN);
        return LocalTime.of(t.getHour(), m);
    }
    private static LocalTime snapUpTo10(LocalTime t) {
        int mod = t.getMinute() % SLOT_MIN;
        int add = (mod == 0) ? 0 : (SLOT_MIN - mod);
        LocalTime r = t.plusMinutes(add);
        return LocalTime.of(r.getHour(), r.getMinute());
    }

    private static String fmt(LocalTime t) {
        return t == null ? null : String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

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

    /**
     * 특정 멤버의 오늘자 스케줄 조회
     */
    public List<WorkSchedule> getTodaySchedulesByMember(Long memberId) {
        LocalDate today = LocalDate.now();
        return getSchedulesByMemberAndDate(memberId, today);
    }

    /**
     * 특정 멤버의 특정 날짜 스케줄 조회
     */
    public List<WorkSchedule> getSchedulesByMemberAndDate(Long memberId, LocalDate date) {
        return workScheduleRepository.findByMember_IdAndWorkDate(memberId, date);
    }

    /**
     * 여러 멤버의 오늘자 스케줄을 memberId 기준으로 묶어서 반환
     */
    public Map<Long, List<WorkSchedule>> getTodaySchedulesByMembers(List<Long> memberIds) {
        LocalDate today = LocalDate.now();
        return getSchedulesByMembersAndDate(memberIds, today);
    }

    /**
     * 여러 멤버의 특정 날짜 스케줄을 memberId 기준으로 묶어서 반환
     */
    public Map<Long, List<WorkSchedule>> getSchedulesByMembersAndDate(List<Long> memberIds, LocalDate date) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<WorkSchedule> list =
                workScheduleRepository.findByMember_IdInAndWorkDate(memberIds, date);

        // memberId 별로 그룹핑
        return list.stream()
                .collect(Collectors.groupingBy(ws -> ws.getMember().getId()));
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

