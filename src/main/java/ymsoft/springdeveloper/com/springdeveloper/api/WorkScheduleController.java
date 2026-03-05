package ymsoft.springdeveloper.com.springdeveloper.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymsoft.springdeveloper.com.springdeveloper.dto.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.entity.WorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.service.ActualWorkScheduleService;
import ymsoft.springdeveloper.com.springdeveloper.service.WorkScheduleService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule/work")
public class WorkScheduleController {

    @Autowired
    private final WorkScheduleService workScheduleService;

    @PostMapping("/generate")
    public ResponseEntity<ScheduleGenerateResponse> generate(
            @Valid @RequestBody ScheduleGenerateRequest request
    ) {
        log.info("[일정생성] memberId={}, 기간={} ~ {}, 항목수={}",
                request.getMemberId(), request.getFrom(), request.getTo(),
                request.getItems() != null ? request.getItems().size() : 0);
        ScheduleGenerateResponse resp = workScheduleService.generateSchedules(request);
        log.info("[일정생성] 완료 - 생성={}, 스킵={}, 덮어쓰기={}",
                resp.getCreated(), resp.getSkipped(), resp.getOverwritten());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/update")
    public ResponseEntity<ScheduleDayUpdateResponse> updateDay(
            @Valid @RequestBody ScheduleDayUpdateRequest request
    ) {
        log.info("[일정수정] memberId={}, 날짜={}, 세그먼트수={}",
                request.getMemberId(), request.getDate(),
                request.getSegments() != null ? request.getSegments().size() : 0);
        ScheduleDayUpdateResponse res = workScheduleService.upsertDay(request);
        log.info("[일정수정] 완료 - memberId={}, 날짜={}, 저장세그먼트={}, 총{}분",
                res.getMemberId(), res.getDate(), res.getCount(), res.getMinutes());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/batchupdate")
    public ResponseEntity<?> updateDay(
            @Valid @RequestBody ScheduleBatchUpdateReqeust request
    ) {
        log.info("[일정일괄수정] memberId={}, 날짜수={}",
                request.getMemberId(),
                request.getDays() != null ? request.getDays().size() : 0);
        List<ScheduleDayUpdateResponse> response = workScheduleService.batchUpdatePlanWeek(request).stream().toList();
        log.info("[일정일괄수정] 완료 - 처리된 날짜수={}", response.size());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{memberId}/{date}")
    public ResponseEntity<Map<String, Object>> getWorkSchedule(
            @PathVariable Long memberId,
            @PathVariable String date
    ) {
        LocalDate workDate = LocalDate.parse(date);
        log.info("[일정조회] memberId={}, 날짜={}", memberId, date);

        List<WorkSchedule> schedules;
        try {
            schedules = workScheduleService.getSchedulesByMemberAndDate(memberId, workDate);
        } catch (Exception e) {
            log.error("[일정조회] DB 조회 실패 memberId={}, 날짜={}: {}", memberId, workDate, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }

        // 데이터 없을 때
        if (schedules == null || schedules.isEmpty()) {
            log.info("[일정조회] memberId={}, 날짜={} - 데이터 없음", memberId, workDate);
            Map<String, Object> res = new HashMap<>();
            res.put("success", false);
            res.put("message", "해당일 근무 데이터 없음");
            res.put("memberId", memberId);
            res.put("date", workDate.toString());
            res.put("segments", Collections.emptyList());
            res.put("totalMinutes", 0);
            return ResponseEntity.ok(res);
        }

        // ✅ segments: [{start:"HH:mm", end:"HH:mm"}] 리스트로 만들기
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        List<Map<String, String>> segments = schedules.stream()
                .map(ws -> {
                    Map<String, String> seg = new HashMap<>();
                    seg.put("start", ws.getStart().format(timeFmt));
                    seg.put("end", ws.getEnd().format(timeFmt));
                    return seg;
                })
                .toList();

        // ✅ 총 근무 시간(분) 합계
        int totalMinutes = schedules.stream()
                .mapToInt(WorkSchedule::getMinutes)
                .sum();

        log.info("[일정조회] memberId={}, 날짜={} - 세그먼트={}, 총{}분",
                memberId, workDate, segments, totalMinutes);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("memberId", memberId);
        res.put("date", workDate.toString());
        res.put("segments", segments);
        res.put("totalMinutes", totalMinutes);

        return ResponseEntity.ok(res);
    }


    @GetMapping("/{memberId}/{start}/{end}")
    public ResponseEntity<ScheduleRangeResponse> getWork(
            @PathVariable Long memberId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        log.info("[기간일정조회] memberId={}, 기간={} ~ {}", memberId, start, end);
        try {
            ScheduleRangeResponse resp = workScheduleService.getWorkRange(memberId, start, end);
            log.info("[기간일정조회] 결과 - {}일 데이터", resp.getDays() != null ? resp.getDays().size() : 0);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("[기간일정조회] DB 조회 실패 memberId={}, {}-{}: {}", memberId, start, end, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /** ✅ JSON 문자열 → List<Map<String,String>> 변환 (segments_json 파싱) */
    private List<Map<String, String>> parseSegmentsJson(String json) {
        try {
            if (json == null || json.isBlank()) return Collections.emptyList();
            // Jackson ObjectMapper 사용
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, List.class);
        } catch (Exception e) {
            log.error("❌ JSON 파싱 오류", e);
            return Collections.emptyList();
        }
    }


    @PostMapping("/{memberId}/delete")
    public ResponseEntity<Map<String, Object>> deleteWorkScheduleByMemberIdWithDay(
            @PathVariable Long memberId,
            @RequestParam String date
    ) {
        log.info("GET /api/schedule/work/{}/delete", memberId);
        Map<String, Object> result = new HashMap<>();

        LocalDate workDate;
        try {
            workDate = LocalDate.parse(date); // yyyy-MM-dd 포맷 가정
        } catch (DateTimeParseException e) {
            result.put("success", false);
            result.put("message", "잘못된 날짜 형식입니다. yyyy-MM-dd 형식으로 보내주세요.");
            result.put("date", date);
            return ResponseEntity.badRequest().body(result);
        }

        long deletedCount = workScheduleService.deleteWorkScheduleByMemberAndDate(memberId, workDate);

        result.put("success", true);
        result.put("memberId", memberId);
        result.put("date", workDate.toString());
        result.put("deletedCount", deletedCount);

        return ResponseEntity.ok().build();
    }
//    @GetMapping("/today")
//    public ResponseEntity<?> getTodayList() {
//        return ResponseEntity.ok(service.getTodaySchedules());
//    }
//
//    @GetMapping("/{memberId}/{date}")
//    public ResponseEntity<?> getMemberDay(
//            @PathVariable Long memberId,
//            @PathVariable String date
//    ) {
//        return ResponseEntity.ok(
//                service.getByMemberAndDate(memberId, LocalDate.parse(date))
//        );
//    }
}
