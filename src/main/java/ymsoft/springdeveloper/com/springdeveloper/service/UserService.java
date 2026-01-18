package ymsoft.springdeveloper.com.springdeveloper.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.UserDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Users;
import ymsoft.springdeveloper.com.springdeveloper.repository.UsersRepository;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(UserDto dto) {

        // 1) 닉네임 중복 체크
        usersRepository.findByNickname(dto.getNickname())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
                });

        // 2) 이메일이 입력되어 있으면 이메일 중복 체크
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usersRepository.findByEmail(dto.getEmail())
                    .ifPresent(u -> {
                        throw new IllegalArgumentException("이미 가입된 이메일입니다.");
                    });
        }

        // 3) 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encodedPassword);
        // 4) 동의 여부 null 방지
        dto.setAgreedTerms((dto.getAgreedTerms() != null) ? dto.getAgreedTerms() : Boolean.FALSE);

        // 6) 저장
        Users saved = usersRepository.save(UserDto.toEntity(dto));
        return saved.getId();
    }

    /**
     * 로그인 (아이디=nickname, 비밀번호 검증)
     * 실패 시 BadCredentialsException 발생
     */
    public Users signin(String email, String rawPassword) {
        // 1) 닉네임으로 사용자 조회
        Users user = usersRepository.findByEmail(email).orElse(null);

        // 2) 비밀번호 매칭 검증
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.info("비밀번호가 일치하지 않습니다.");
            return null;
        }

        // 3) 성공 시 User 반환 (컨트롤러/세션에서 활용)
        return user;
    }

    // 빈 문자열을 null로 치환 (옵션 필드 편의용)
    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByEmail(username).orElseThrow(()-> new IllegalArgumentException(username));
    }
}
