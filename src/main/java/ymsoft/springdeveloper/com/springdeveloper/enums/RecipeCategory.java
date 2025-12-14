package ymsoft.springdeveloper.com.springdeveloper.enums;

public enum RecipeCategory {

    HOT_COFFEE("Hot 커피"),
    ICE_COFFEE("Ice 커피"),
    HOT_LATTE("Hot 라떼"),
    ICE_LATTE("Ice 라떼"),
    ICE_NON_COFFEE("Ice 논커피 라떼"),
    ICE_NON_COFFEE_HUKDANG("흑당"),
    HOT_NON_COFFEE("Hot 논커피 라떼"),
    HOT_COLD_BREW("Hot 콜드브루"),
    HOT_LATTE_COLD_BREW("Hot 콜드브루라떼"),
    ICE_COLD_BREW("Ice 콜드브루"),
    ICE_LATTE_COLD_BREW("Ice 콜드브루라떼"),
    SMOOTHIE_ADE("스무디"),
    ADE("에이드"),
    JUICE("주스"),
    FRAPPE("프라페"),
    PONG_CRUSH("퐁크러쉬"),
    ICE_TEA("아이스티"),
    HOT_TEA("핫티"),
    FOOD("푸드"),
    BASE("base");

    private final String displayName;

    RecipeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RecipeCategory fromDisplayName(String displayName) {
        for (RecipeCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 카테고리: " + displayName);
    }
}
