package ymsoft.springdeveloper.com.springdeveloper.dto;

import java.util.List;

public record DayWorkDto(
        String date,                 // "YYYY-MM-DD"
        List<WorkSegmentDto> segments,
        int minutes                  // 실 근무 총 분
) {}
