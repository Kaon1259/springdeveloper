package ymsoft.springdeveloper.com.springdeveloper.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import ymsoft.springdeveloper.com.springdeveloper.entity.Users;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserDto {

    private Long id;

    /** 닉네임 (필수) */
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String nickname;

    /** 비밀번호 (필수) */
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    /** 비밀번호 확인 (필수) */
    @NotBlank(message = "비밀번호 확인을 입력해주세요.")
    private String passwordConfirm;

    /** 이메일 (선택) */
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    /** 전화번호 (선택) */
    private String phone;

    /** 이용약관 동의 (필수, checkbox) */
    private Boolean agreedTerms;

    /** 체크박스 값 변환용 */
    public void applyTermsCheckbox(String agreeValue) {
        this.agreedTerms = "Y".equalsIgnoreCase(agreeValue);
    }

    /** 비밀번호 일치 체크 */
    public boolean isPasswordMatched() {
        return password != null && password.equals(passwordConfirm);
    }

    public static UserDto toDto(Users entity) {
        return UserDto.builder()
                .id(entity.getId())
                .nickname(entity.getNickname())
                .password(entity.getPassword())
                .passwordConfirm(entity.getPassword())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .agreedTerms(entity.getAgreedTerms())
                .build();
    }

    public static Users toEntity(UserDto dto) {
        return Users.builder()
                .nickname(dto.getNickname())
                .password(dto.getPassword())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .agreedTerms(dto.getAgreedTerms())
                .build();
    }
}
