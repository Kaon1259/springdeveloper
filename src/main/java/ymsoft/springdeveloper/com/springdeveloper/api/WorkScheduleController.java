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
        ScheduleGenerateResponse resp = workScheduleService.generateSchedules(request);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/update")
    public ResponseEntity<ScheduleDayUpdateResponse> updateDay(
            @Valid @RequestBody ScheduleDayUpdateRequest request
    ) {
        ScheduleDayUpdateResponse res = workScheduleService.upsertDay(request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{memberId}/{date}")
    public ResponseEntity<Map<String, Object>> getWorkSchedule(
            @PathVariable Long memberId,
            @PathVariable String date
    ) {
        LocalDate workDate = LocalDate.parse(date);
        log.info("GET /api/schedule/{}/{}", memberId, date);

        // ✅ 서비스에서 List<WorkSchedule> 받아오기
        List<WorkSchedule> schedules = workScheduleService.getSchedulesByMemberAndDate(memberId, workDate);

        // 데이터 없을 때
        if (schedules == null || schedules.isEmpty()) {
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
        log.info("memberId={}, start={}, end={}", memberId, start, end);
        return ResponseEntity.ok(workScheduleService.getWorkRange(memberId, start, end));
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
