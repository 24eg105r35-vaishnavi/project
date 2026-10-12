package app.model;

public enum AchievementLevel {
    INTRA_COLLEGE("Intra-College"),
    INTER_COLLEGE("Inter-College"),
    STATE("State Level"),
    NATIONAL("National Level"),
    INTERNATIONAL("International Level");

    private final String displayName;

    AchievementLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
