package ymsoft.springdeveloper.com.springdeveloper.dto;


public enum MemberStatus {
    //WAITING, WORKING, RESTING, PAUSED, RESIGNED
    WAITING("대기 중"),
    WORKING("근무 중"),
    RESTING("휴식 중"),
    PAUSED("일시 중지"),
    RESIGNED("퇴사");

    // Enum 상수와 연결될 한국어 이름 필드
    private final String displayName;

    // 생성자: Enum 상수 정의 시 displayName을 초기화합니다.
    MemberStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Enum 값을 문자열로 변환할 때 사용자 친화적인 한국어 이름을 반환하도록 오버라이드합니다.
     * 스프링에서 DTO를 JSON으로 직렬화하거나 템플릿 엔진에서 사용할 때 유용합니다.
     * * @return 해당 상태의 한국어 이름
     */
    @Override
    public String toString() {
        return this.displayName;
    }

    /**
     * (선택 사항) 필드를 직접 가져오고 싶을 때 사용하는 Getter입니다.
     */
    public String getDisplayName() {
        return displayName;
    }
}

