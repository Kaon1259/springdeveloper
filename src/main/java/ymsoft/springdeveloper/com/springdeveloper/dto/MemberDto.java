package ymsoft.springdeveloper.com.springdeveloper.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Builder
@Data
@Getter
@Setter
public class MemberDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String gender; // '남성'/'여성' (필요 시 enum으로 대체)

    @NotNull
    private LocalDate startDate;

    @NotBlank
    private String phone;

    private String email;

    @NotNull
    @Min(0)
    private Integer hourlyWage;

    private Boolean hasHealthCertificate;   // null 가능 → Boolean.TRUE.equals(...)로 처리
    private LocalDate healthCertExpiry;     // 선택

    private String bankName;
    private String bankAccount;

    @NotNull
    private Member.Status status;           // Member 엔티티의 Status enum 사용

    private List<ScheduleRow> schedule = new ArrayList<>();

    /** 등록일, 수정일 추가 **/
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    @Data
    public static class ScheduleRow {
        @NotNull
        private WeekDay day;      // MON, TUE, ...
        @NotNull
        private LocalTime start;  // "HH:mm"
        @NotNull
        private LocalTime end;    // "HH:mm"

        public String getDayLabel() {
            return switch (day) {
                case MON ->  "(MON)월";
                case TUE -> "(TUE)화";
                case WED -> "(WED)수";
                case THU -> "(THU)목";
                case FRI -> "(FRI)금";
                case SAT -> "(SAT)토";
                case SUN -> "(SUN)일";
            };
        }
    }

    // List<Member> → List<MemberDto>
    public static List<MemberDto> toDtoList(List<Member> members) {
        return members.stream()
                .map(MemberDto::fromEntity)
                .collect(Collectors.toList());
    }

    // ScheduleItem → ScheduleRow 변환
    private static ScheduleRow toScheduleRow(ScheduleItem s) {
        return ScheduleRow.builder()
                .day(WeekDay.valueOf(s.getDay().name()))
                .start(s.getStart())
                .end(s.getEnd())
                .build();
    }

    // Member → MemberDto 변환 (단일)
    public static MemberDto fromEntity(Member m) {
        return MemberDto.builder()
                .id(m.getId())
                .name(m.getName())
                .gender(m.getGender())
                .startDate(m.getStartDate())
                .phone(m.getPhone())
                .email(m.getEmail())
                .hourlyWage(m.getHourlyWage())
                .hasHealthCertificate(m.getHasHealthCertificate())
                .healthCertExpiry(m.getHealthCertExpiry())
                .bankName(m.getBankName())
                .bankAccount(m.getBankAccount())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .schedule(
                        Optional.ofNullable(m.getSchedules())
                                .orElse(List.of())
                                .stream()
                                .map(MemberDto::toScheduleRow)
                                .collect(Collectors.toList())
                )
                .build();
    }

    public static Member toEntity(MemberDto dto) {
        Member member = Member.builder()
                .id(dto.getId())
                .name(dto.getName())
                .gender(dto.getGender())
                .startDate(dto.getStartDate())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .hourlyWage(dto.getHourlyWage())
                .hasHealthCertificate(Boolean.TRUE.equals(dto.getHasHealthCertificate()))
                .healthCertExpiry(dto.getHealthCertExpiry())
                .bankName(dto.getBankName())
                .bankAccount(dto.getBankAccount())
                .status(dto.getStatus())
                .build();


        if (dto.getSchedule() != null && !dto.getSchedule().isEmpty()) {
            log.info(dto.getSchedule().toString());
            List<ScheduleItem> items = dto.getSchedule().stream()
                    .map(r -> ScheduleItem.builder()
                            .day(ScheduleItem.WeekDay.valueOf(r.getDay().name())) // WeekDay → ScheduleItem.Day
                            .start(r.getStart())
                            .end(r.getEnd())
                            .member(member)
                            .build())
                    .collect(Collectors.toList());
            member.setSchedules(items);
        }

        return member;
    }
}
