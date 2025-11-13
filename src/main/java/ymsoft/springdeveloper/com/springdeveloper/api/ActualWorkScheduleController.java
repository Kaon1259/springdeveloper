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
import ymsoft.springdeveloper.com.springdeveloper.service.ActualWorkScheduleService;
import ymsoft.springdeveloper.com.springdeveloper.service.WorkScheduleService;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ActualWorkScheduleController {

    @Autowired
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

    @PostMapping("/update")
    public ResponseEntity<Void> updateActualWork(@Valid @RequestBody UpdateWorkScheduleRequest request) {

        log.info("Received update request for memberId={}, date={}", request.getMemberId(), request.getDate());
        service.updateActualWorkSchedule(request);

        log.info("Return for Update request for memberId={}, date={}", request.getMemberId(), request.getDate());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/actual-bulk-register")
    public ResponseEntity<Map<String, Object>> saveActualBulk(
            @RequestBody @Valid ActualWorkScheduleBulkDto request
    ) {
        log.info(request.toString());
        if (request.getSegments() == null || request.getSegments().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "segments가 비어 있습니다."
            ));
        }

        log.info(request.getSegments().toString());
        List<ActualWorkSchedule> savedList = new ArrayList<>();

        for (ActualWorkScheduleBulkDto.Item item : request.getSegments()) {
            if (item == null) continue;
            if (item.getMemberId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "memberId가 누락된 항목이 있습니다."
                ));
            }
            if (item.getSegments() == null) { // ✅ 여기서 NPE 방어
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "memberId=" + item.getMemberId() + " 의 segments가 누락되었습니다."
                ));
            }

            // DTO를 이용해 엔티티 생성/저장
            ActualWorkScheduleDto dto = new ActualWorkScheduleDto(
                    item.getMemberId(),
                    request.getDate(),
                    item.getSegments()
            );
            ActualWorkSchedule entity = dto.toEntity();
            savedList.add(service.save(entity));
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "총 " + savedList.size() + "건 저장 완료");
        res.put("ids", savedList.stream().map(ActualWorkSchedule::getId).collect(Collectors.toList()));
        log.info(res.toString());
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

    // /api/schedule/{memberId}/{start}/{end}
    @GetMapping("/{memberId}/{start}/{end}")
    public ResponseEntity<WeekWorkResponse> getWorklogs(
            @PathVariable Long memberId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        WeekWorkResponse body = service.getWeek(memberId, start, end);

        log.info("GET /api/schedule/{}/{}", memberId, start);
        log.info("WeekWorkResponse {}", body.toString());

        return ResponseEntity.ok(body);
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
