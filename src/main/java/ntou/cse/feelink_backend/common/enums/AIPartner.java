package ntou.cse.feelink_backend.common.enums;

public enum AIPartner {
    MUSE("謬思", "Muse"),
    QIQI("奇奇", "QiQi"),
    NUANNUAN("暖暖", "NuanNuan");

    private final String chineseName;
    private final String englishName;

    AIPartner(String chineseName, String englishName) {
        this.chineseName = chineseName;
        this.englishName = englishName;
    }

    public String getDisplayName() { return chineseName; }
    public String getChineseName() { return chineseName; }
    public String getEnglishName() { return englishName; }

    public static AIPartner fromName(String name) {
        for (AIPartner partner : values()) {
            if (partner.chineseName.equals(name)) {
                return partner;
            }
        }
        for (AIPartner partner : values()) {
            if (partner.englishName.equalsIgnoreCase(name)) {
                return partner;
            }
        }
        throw new IllegalArgumentException("Unknown AI Partner: " + name);
    }

    public static AIPartner fromChineseName(String name) {
        for (AIPartner partner : values()) {
            if (partner.chineseName.equals(name)) {
                return partner;
            }
        }
        throw new IllegalArgumentException("Unknown AI Partner: " + name);
    }

    public static AIPartner fromEnglishName(String name) {
        for (AIPartner partner : values()) {
            if (partner.englishName.equalsIgnoreCase(name)) {
                return partner;
            }
        }
        throw new IllegalArgumentException("Unknown AI Partner: " + name);
    }

    // toString 預設回傳中文
    @Override
    public String toString() {
        return chineseName;
    }
}