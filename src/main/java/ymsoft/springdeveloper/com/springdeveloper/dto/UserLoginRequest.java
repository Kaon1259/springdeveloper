package ymsoft.springdeveloper.com.springdeveloper.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserLoginRequest {

    /** 로그인 아이디 (이메일이든 닉네임이든, 로그인 폼에서 name="id") */
    @NotBlank(message = "이메일을 입력해 주세요.")
    private String email;

    /** 비밀번호 */
    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;
}
