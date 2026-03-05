package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.mapper.MemberMapper;
import ymsoft.springdeveloper.com.springdeveloper.mapper.WorkScheduleMapper;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkScheduleService {

    private final MemberMapper memberMapper;
    private final WorkScheduleMapper workScheduleMapper;

    private static final int SLOT_MIN = 10;
    private static final LocalTime START_BOUND = LocalTime.of(6, 0);
    private static final LocalTime END_BOUND = LocalTime.of(22, 0);

    public List<WorkSchedule> findByWorkDateWithMember(LocalDate date) {
        log.info("[근무조회] 날짜={} 전체 근무 스케줄 조회", date);
        List<WorkSchedule> result = workScheduleMapper.findByWorkDateWithMember(date);
        log.info("[근무조회] 날짜={} - {}건 조회됨", date, result.size());
        return result;
    }

    @Transactional
    public ScheduleDayUpdateResponse upsertDay(ScheduleDayUpdateRequest req) {
        final Long memberId = req.getMemberId();
        final LocalDate date = req.getDate();

        log.info("[일정저장] memberId={}, 날짜={}, 세그먼트수={}", memberId, date,
                req.getSegments() != null ? req.getSegments().size() : 0);

        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 memberId: " + memberId));

        List<ScheduleDayUpdateRequest.Segment> normalized = normalizeAndValidate(req.getSegments());

        workScheduleMapper.deleteByMemberIdAndWorkDate(memberId, date);

        Set<String> seen = new HashSet<>();
        List<WorkSchedule> toSave = new ArrayList<>();
        for (ScheduleDayUpdateRequest.Segment seg : normalized) {
            String key = seg.getStart() + "|" + seg.getEnd();
            if (!seen.add(key)) continue;

            WorkSchedule ws = WorkSchedule.builder()
                    .memberId(memberId)
                    .workDate(date)
                    .start(seg.getStart())
                    .end(seg.getEnd())
                    .source(WorkSchedule.SourceType.MANUAL)
                    .note(seg.getNote())
                    .build();
            toSave.add(ws);
        }

        if (toSave.isEmpty()) {
            log.info("[일정저장] memberId={}, 날짜={} - 저장할 세그먼트 없음 (기존 삭제만 처리)", memberId, date);
            return ScheduleDayUpdateResponse.builder()
                    .memberId(memberId).date(date).count(0).minutes(0).segments(List.of()).build();
        }

        for (WorkSchedule ws : toSave) {
            workScheduleMapper.insert(ws);
        }
        log.info("[일정저장] memberId={}, 날짜={} - {}개 세그먼트 저장 완료", memberId, date, toSave.size());

        int minutes = toSave.stream().mapToInt(s -> diffMinutes(s.getStart(), s.getEnd())).sum();

        List<ScheduleDayUpdateResponse.Segment> outSegs = toSave.stream()
                .sorted(Comparator.comparing(WorkSchedule::getStart))
                .map(s -> ScheduleDayUpdateResponse.Segment.builder()
                        .start(fmt(s.getStart())).end(fmt(s.getEnd())).note(s.getNote()).build())
                .toList();

        return ScheduleDayUpdateResponse.builder()
                .memberId(memberId).date(date).count(toSave.size()).minutes(minutes).segments(outSegs).build();
    }

    private List<ScheduleDayUpdateRequest.Segment> normalizeAndValidate(List<ScheduleDayUpdateRequest.Segment> segments) {
        if (segments == null || segments.isEmpty()) return List.of();

        List<ScheduleDayUpdateRequest.Segment> clipped = new ArrayList<>();
        for (ScheduleDayUpdateRequest.Segment s : segments) {
            LocalTime st = s.getStart();
            LocalTime en = s.getEnd();
            if (st == null || en == null) continue;
            if (st.isBefore(START_BOUND)) st = START_BOUND;
            if (en.isAfter(END_BOUND)) en = END_BOUND;
            if (!st.isBefore(en)) continue;
            st = snapDownTo10(st);
            en = snapUpTo10(en);
            if (!st.isBefore(en)) continue;
            if (!isTenMinUnit(st) || !isTenMinUnit(en)) {
                throw new IllegalArgumentException("구간은 10분 단위여야 합니다. (" + st + "~" + en + ")");
            }
            clipped.add(ScheduleDayUpdateRequest.Segment.builder().start(st).end(en).note(s.getNote()).build());
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
            if (!s2.isAfter(curE)) {
                if (e2.isAfter(curE)) curE = e2;
            } else if (s2.equals(curE)) {
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
        Member member = memberMapper.findById(req.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 근로자(memberId=" + req.getMemberId() + ") 입니다."));

        int totalRequested = req.getItems().size();
        int created = 0, skipped = 0, overwritten = 0;
        List<Long> createdIds = new ArrayList<>();
        List<ScheduleGenerateResponse.Conflict> conflicts = new ArrayList<>();

        if (req.getFrom().isAfter(req.getTo())) {
            throw new IllegalArgumentException("from 은 to 보다 이후일 수 없습니다.");
        }

        for (ScheduleGenerateRequest.Item item : req.getItems()) {
            LocalDate date = item.getDate();
            LocalTime start = item.getStart();
            LocalTime end = item.getEnd();

            if (date.isBefore(req.getFrom()) || date.isAfter(req.getTo())) {
                skipped++;
                conflicts.add(ScheduleGenerateResponse.Conflict.builder()
                        .date(String.valueOf(date)).start(start != null ? start.toString() : null)
                        .end(end != null ? end.toString() : null).existingId(null).reason("OUT_OF_RANGE").build());
                continue;
            }

            String invalidReason = validateTimes(start, end);
            if (invalidReason != null) {
                skipped++;
                conflicts.add(ScheduleGenerateResponse.Conflict.builder()
                        .date(date.toString()).start(start != null ? start.toString() : null)
                        .end(end != null ? end.toString() : null).existingId(null).reason(invalidReason).build());
                continue;
            }

            List<WorkSchedule> existing = workScheduleMapper.findByMemberIdAndWorkDate(member.getId(), date);
            List<WorkSchedule> overlapped = existing.stream()
                    .filter(ws -> isOverlap(start, end, ws.getStart(), ws.getEnd())).toList();

            if (!overlapped.isEmpty()) {
                if (req.isOverwrite()) {
                    List<Long> ids = overlapped.stream().map(WorkSchedule::getId).collect(Collectors.toList());
                    workScheduleMapper.deleteAll(ids);
                    overwritten += overlapped.size();
                    WorkSchedule saved = saveOne(member.getId(), date, start, end, item.getNote());
                    created++;
                    createdIds.add(saved.getId());
                } else {
                    skipped++;
                    Long firstId = overlapped.get(0).getId();
                    conflicts.add(ScheduleGenerateResponse.Conflict.builder()
                            .date(date.toString()).start(start.toString()).end(end.toString())
                            .existingId(firstId).reason("DUPLICATED").build());
                }
            } else {
                WorkSchedule saved = saveOne(member.getId(), date, start, end, item.getNote());
                created++;
                createdIds.add(saved.getId());
            }
        }

        return ScheduleGenerateResponse.builder()
                .totalRequested(totalRequested).created(created).skipped(skipped)
                .overwritten(overwritten).createdIds(createdIds).conflicts(conflicts).build();
    }

    private WorkSchedule saveOne(Long memberId, LocalDate date, LocalTime start, LocalTime end, String note) {
        WorkSchedule ws = WorkSchedule.builder()
                .memberId(memberId).workDate(date).start(start).end(end)
                .source(WorkSchedule.SourceType.GENERATED).note(note).build();
        workScheduleMapper.insert(ws);
        return ws;
    }

    public ScheduleRangeResponse getWorkRange(Long memberId, LocalDate start, LocalDate end) {
        validateParams(memberId, start, end);
        if (!memberMapper.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 근로자입니다. memberId=" + memberId);
        }
        log.info("[기간일정] memberId={}, 조회기간={} ~ {}", memberId, start, end);

        List<WorkSchedule> schedules =
                workScheduleMapper.findByMemberIdAndWorkDateBetweenOrderByWorkDateAscStartAsc(memberId, start, end);
        log.info("[기간일정] memberId={} - {}건 조회됨", memberId, schedules.size());

        Map<LocalDate, List<WorkSchedule>> byDate = schedules.stream()
                .collect(Collectors.groupingBy(WorkSchedule::getWorkDate, LinkedHashMap::new, Collectors.toList()));

        ScheduleRangeResponse resp = new ScheduleRangeResponse();
        byDate.forEach((date, list) -> {
            List<ScheduleRangeResponse.Seg> segs = list.stream()
                    .map(ws -> ScheduleRangeResponse.Seg.builder()
                            .start(ws.getStart()).end(ws.getEnd()).note(ws.getNote()).build())
                    .collect(Collectors.toList());
            int minutes = list.stream().mapToInt(ws -> diffMinutes(ws.getStart(), ws.getEnd()))
                    .filter(m -> m > 0).sum();
            resp.getDays().add(ScheduleRangeResponse.Day.builder()
                    .date(date).segments(segs).minutes(minutes).build());
        });
        return resp;
    }

    public List<WorkSchedule> getWeeklyWork(Long memberId, LocalDate start, LocalDate end) {
        validateParams(memberId, start, end);
        if (!memberMapper.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 근로자입니다. memberId=" + memberId);
        }
        return workScheduleMapper.findByMemberIdAndWorkDateBetweenOrderByWorkDateAscStartAsc(memberId, start, end);
    }

    @Transactional
    public List<ScheduleDayUpdateResponse> batchUpdatePlanWeek(ScheduleBatchUpdateReqeust req) {
        List<ScheduleDayUpdateResponse> response = new ArrayList<>();
        req.getDays().forEach(planDay -> {
            List<ScheduleDayUpdateRequest.Segment> segments =
                    (planDay.getSegments() == null) ? List.of() :
                    planDay.getSegments().stream()
                            .map(seg -> ScheduleDayUpdateRequest.Segment.builder()
                                    .start(LocalTime.parse(seg.getStart()))
                                    .end(LocalTime.parse(seg.getEnd()))
                                    .note(null).build())
                            .collect(Collectors.toList());

            ScheduleDayUpdateRequest dayReq = ScheduleDayUpdateRequest.builder()
                    .memberId(req.getMemberId()).date(planDay.getDate()).segments(segments).build();
            response.add(upsertDay(dayReq));
        });
        return response;
    }

    public Integer deleteWorkScheduleByMemberAndDate(Long memberId, LocalDate date) {
        List<WorkSchedule> schedules = workScheduleMapper.findByMemberIdAndWorkDate(memberId, date);
        if (!schedules.isEmpty()) {
            List<Long> ids = schedules.stream().map(WorkSchedule::getId).collect(Collectors.toList());
            workScheduleMapper.deleteAll(ids);
        }
        return schedules.size();
    }

    public List<WorkSchedule> getTodaySchedulesByMember(Long memberId) {
        return getSchedulesByMemberAndDate(memberId, LocalDate.now());
    }

    public List<WorkSchedule> getSchedulesByMemberAndDate(Long memberId, LocalDate date) {
        return workScheduleMapper.findByMemberIdAndWorkDate(memberId, date);
    }

    public Map<Long, List<WorkSchedule>> getTodaySchedulesByMembers(List<Long> memberIds) {
        return getSchedulesByMembersAndDate(memberIds, LocalDate.now());
    }

    public Map<Long, List<WorkSchedule>> getSchedulesByMembersAndDate(List<Long> memberIds, LocalDate date) {
        if (memberIds == null || memberIds.isEmpty()) return Map.of();
        List<WorkSchedule> list = workScheduleMapper.findByMemberIdInAndWorkDate(memberIds, date);
        return list.stream().collect(Collectors.groupingBy(WorkSchedule::getMemberId));
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

    private boolean isOverlap(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

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
