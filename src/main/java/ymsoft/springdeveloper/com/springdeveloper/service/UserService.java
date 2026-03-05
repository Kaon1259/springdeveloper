package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.UserDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Users;
import ymsoft.springdeveloper.com.springdeveloper.mapper.UsersMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final String ALLOWED_EMAIL    = "kaon1259@naver.com";
    private static final String ALLOWED_PASSWORD = "molly2204!@";

    private final UsersMapper usersMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signup(UserDto dto) {
        usersMapper.findByNickname(dto.getNickname())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 닉네임입니다."); });

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            usersMapper.findByEmail(dto.getEmail())
                    .ifPresent(u -> { throw new IllegalArgumentException("이미 가입된 이메일입니다."); });
        }

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        dto.setPassword(encodedPassword);
        dto.setAgreedTerms(Boolean.TRUE.equals(dto.getAgreedTerms()));

        Users users = UserDto.toEntity(dto);
        usersMapper.insert(users);
        return users.getId();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUsername: {}", email);
        if (!ALLOWED_EMAIL.equalsIgnoreCase(email)) {
            log.warn("로그인 거부 - 허용되지 않은 계정: {}", email);
            throw new UsernameNotFoundException("접근이 허용되지 않은 계정입니다.");
        }
        return User.withUsername(ALLOWED_EMAIL)
                .password(passwordEncoder.encode(ALLOWED_PASSWORD))
                .roles("USER")
                .build();
    }
}
