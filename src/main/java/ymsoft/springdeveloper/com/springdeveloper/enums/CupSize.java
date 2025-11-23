package ymsoft.springdeveloper.com.springdeveloper.enums;


public enum CupSize {

    OZ20(20),
    OZ24(24),
    OZ32(32);

    private final int ounce;

    CupSize(int ounce) {
        this.ounce = ounce;
    }

    public int getOunce() {
        return ounce;
    }

    public static CupSize fromOunce(int ounce) {
        switch (ounce) {
            case 20:
                return OZ20;
            case 24:
                return OZ24;
            case 32:
                return OZ32;
            default:
                throw new IllegalArgumentException("지원하지 않는 컵 용량: " + ounce);
        }
    }
}
