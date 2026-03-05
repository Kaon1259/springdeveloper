package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.entity.ScheduleItem;
import ymsoft.springdeveloper.com.springdeveloper.mapper.MemberMapper;
import ymsoft.springdeveloper.com.springdeveloper.mapper.ScheduleItemMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class memberService {

    private final MemberMapper memberMapper;
    private final ScheduleItemMapper scheduleItemMapper;

    @Transactional
    public Member save(MemberDto dto) {
        Member member = MemberDto.toEntity(dto);
        memberMapper.insert(member);

        // 스케줄 아이템 저장
        if (member.getSchedules() != null) {
            for (ScheduleItem item : member.getSchedules()) {
                item.setMemberId(member.getId());
                scheduleItemMapper.insert(item);
            }
        }
        return member;
    }

    public MemberDto findById(Long id) {
        return memberMapper.findWithSchedulesById(id)
                .map(MemberDto::fromEntity)
                .orElse(null);
    }

    public List<MemberDto> findAll() {
        return MemberDto.toDtoList(memberMapper.findAll());
    }

    public List<MemberDto> findByStatus(Member.Status status) {
        return MemberDto.toDtoList(memberMapper.findByStatus(status.name()));
    }

    @Transactional
    public MemberDto update(Long id, MemberDto dto) {
        if (!memberMapper.existsById(id)) return null;
        dto.setId(id);
        Member member = MemberDto.toEntity(dto);
        memberMapper.update(member);

        // 스케줄 아이템 갱신: 기존 전체 삭제 후 재삽입
        for (ScheduleItem.WeekDay day : ScheduleItem.WeekDay.values()) {
            scheduleItemMapper.deleteByMemberIdAndDay(id, day.name());
        }
        if (member.getSchedules() != null) {
            for (ScheduleItem item : member.getSchedules()) {
                item.setMemberId(id);
                scheduleItemMapper.insert(item);
            }
        }
        return memberMapper.findWithSchedulesById(id).map(MemberDto::fromEntity).orElse(null);
    }
}
