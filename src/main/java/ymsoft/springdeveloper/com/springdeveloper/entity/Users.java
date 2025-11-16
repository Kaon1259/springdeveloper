package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 닉네임 (필수, 유니크 권장) */
    @Column(nullable = false, length = 30, unique = true)
    private String nickname;

    /** 비밀번호 (필수, 해시된 값 저장) */
    @Column(nullable = false, length = 255)
    private String password;

    /** 이메일 (선택) */
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    /** 전화번호 (선택) */
    @Column(length = 20)
    private String phone;

    /** 이용약관/개인정보 동의 여부 (체크박스 값) */
    @Column(nullable = false)
    private Boolean agreedTerms;

    /** 생성 일시 */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // === 엔티티 라이프사이클 콜백 ===
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.agreedTerms == null) {
            this.agreedTerms = Boolean.FALSE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
