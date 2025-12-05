package de.oth.muskelmanagement.model.enums;

public enum EquipmentStatus {
    AVAILABLE("Verfügbar"), IN_MAINTENANCE("In Wartung"), DEFECTIVE("Defekt"), ARCHIVED("Archiviert");

    private final String displayName;

    EquipmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
