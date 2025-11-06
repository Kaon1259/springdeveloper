package ymsoft.springdeveloper.com.springdeveloper.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymsoft.springdeveloper.com.springdeveloper.dto.ActualWorkScheduleDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.service.ActualWorkScheduleService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ActualWorkScheduleController {

    private final ActualWorkScheduleService service;

    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveActual(@RequestBody ActualWorkScheduleDto request) {
        log.info("Received save request for memberId={}, date={}", request.getMemberId(), request.getDate());

        ActualWorkSchedule entity = request.toEntity();
        ActualWorkSchedule saved = service.save(entity);

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "저장 완료");
        res.put("id", saved.getId());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{memberId}/{date}")
    public ResponseEntity<Map<String, Object>> getActualSchedule(
            @PathVariable Long memberId,
            @PathVariable String date
    ) {
        LocalDate workDate = LocalDate.parse(date);
        log.info("GET /api/schedule/{}/{}", memberId, date);

        return service.findByMemberAndDate(memberId, workDate)
                .map(entity -> {
                    // ✅ DB에 데이터가 존재하면 DTO 형태로 반환
                    Map<String, Object> res = new HashMap<>();
                    res.put("success", true);
                    res.put("memberId", entity.getMemberId());
                    res.put("date", entity.getWorkDate());
                    res.put("segments", parseSegmentsJson(entity.getSegmentsJson()));
                    res.put("totalMinutes", entity.getTotalMinutes());
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    // ✅ 해당 날짜에 데이터가 없으면 빈 JSON 반환
                    Map<String, Object> res = new HashMap<>();
                    res.put("success", false);
                    res.put("message", "해당일 근무 데이터 없음");
                    res.put("segments", Collections.emptyList());
                    return ResponseEntity.ok(res);
                });
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
