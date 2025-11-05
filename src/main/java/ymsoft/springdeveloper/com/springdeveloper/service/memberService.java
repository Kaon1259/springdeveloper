package ymsoft.springdeveloper.com.springdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.MemberDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;
import ymsoft.springdeveloper.com.springdeveloper.repository.memberRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class memberService {

    @Autowired
    private  memberRepository memberRepository;

    public Member save(MemberDto dto) {
        Member member = MemberDto.toEntity(dto);
        return memberRepository.save(member);
    }

    public MemberDto findById(Long id) {
        Member member = memberRepository.findById(id).orElse(null);

        if (member == null) { return null; }

        return MemberDto.fromEntity(member);
    }

    public List<MemberDto> findAll() {
        return MemberDto.toDtoList((List<Member>) memberRepository.findAll());
    }

    public List<MemberDto> findByStatus(Member.Status status) {
        return MemberDto.toDtoList((List<Member>) memberRepository.findByStatus(status));
    }

    @Transactional
    public MemberDto update(Long id, MemberDto dto){
        Member member = memberRepository.findById(id).orElse(null);
        if (member == null) { return null; }

        Member saved = memberRepository.save(MemberDto.toEntity(dto));

        return MemberDto.fromEntity(saved);
    }
}