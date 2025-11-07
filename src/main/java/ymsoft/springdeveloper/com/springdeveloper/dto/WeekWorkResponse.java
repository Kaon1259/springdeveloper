package ymsoft.springdeveloper.com.springdeveloper.dto;

import java.util.List;

public record WeekWorkResponse(
        String memberId,
        List<DayWorkDto> days
) {}

