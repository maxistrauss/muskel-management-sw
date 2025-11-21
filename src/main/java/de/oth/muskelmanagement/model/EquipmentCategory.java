package de.oth.muskelmanagement.model;

public enum EquipmentCategory {
    CARDIO("Cardio"),
    STRENGTH("Krafttraining"),
    FREE_WEIGHTS("Freihanteln"),
    OTHER("Sonstiges");

    private final String displayName;

    EquipmentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}