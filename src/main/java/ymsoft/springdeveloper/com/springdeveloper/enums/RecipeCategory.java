package ymsoft.springdeveloper.com.springdeveloper.enums;

public enum RecipeCategory {

    HOT_COFFEE("Hot 커피"),
    ICE_COFFEE("Ice 커피"),
    HOT_LATTE("Hot라떼"),
    ICE_LATTE("Ice 라떼"),
    ICE_NON_COFFEE("Ice 논커피"),
    HOT_NON_COFFEE("Hot 논커피"),
    LATTE_COLD_BREW("라떼 콜드브루"),
    SMOOTHIE_ADE("스무디 에이드"),
    JUICE("주스"),
    FRAPPE("프라페");

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
