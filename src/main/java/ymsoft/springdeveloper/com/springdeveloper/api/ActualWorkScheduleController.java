package ymsoft.springdeveloper.com.springdeveloper.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ymsoft.springdeveloper.com.springdeveloper.dto.ActualWorkScheduleDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.ActualWorkSchedule;
import ymsoft.springdeveloper.com.springdeveloper.service.ActualWorkScheduleService;

import java.util.HashMap;
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
