package de.oth.muskelmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    private Room room;

    private LocalDate purchaseDate;

    private String manufacturer;

    @Enumerated(EnumType.STRING)
    private EquipmentCategory category;

    private Integer maintenanceInterval; // in days

    private LocalDate lastMaintenanceDate;

    @Column(nullable = false)
    private boolean archived = false;

    public Equipment() {
    }

    public Equipment(String name, String serialNumber, EquipmentStatus status, Room room,
                     LocalDate purchaseDate, String manufacturer, EquipmentCategory category,
                     Integer maintenanceInterval, LocalDate lastMaintenanceDate) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.status = status;
        this.room = room;
        this.purchaseDate = purchaseDate;
        this.manufacturer = manufacturer;
        this.category = category;
        this.maintenanceInterval = maintenanceInterval;
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public void setCategory(EquipmentCategory category) {
        this.category = category;
    }

    public Integer getMaintenanceInterval() {
        return maintenanceInterval;
    }

    public void setMaintenanceInterval(Integer maintenanceInterval) {
        this.maintenanceInterval = maintenanceInterval;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}