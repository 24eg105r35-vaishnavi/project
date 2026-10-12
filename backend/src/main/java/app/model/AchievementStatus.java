package app.model;

public enum AchievementStatus {
    PENDING("Under Review", "badge-pending"),
    APPROVED("Verified", "badge-approved"),
    REJECTED("Rejected", "badge-rejected"),
    RETURNED_FOR_CORRECTION("Needs Correction", "badge-correction");

    private final String label;
    private final String badgeClass;

    AchievementStatus(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
