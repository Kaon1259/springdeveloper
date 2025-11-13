package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.Data;

import java.util.List;

@Data
public class WeeklyTemplateUpdateRequest {

    private Long memberId;   // body 에 들어온 memberId (path 와 비교해서 검증용으로 써도 됨)
    private String day;      // "MON", "TUE", ... "SUN"
    private List<Segment> segments;

    @Data
    public static class Segment {
        private String start; // "HH:mm"
        private String end;   // "HH:mm"
    }
}
