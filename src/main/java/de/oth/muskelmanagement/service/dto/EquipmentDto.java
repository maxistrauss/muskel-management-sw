package de.oth.muskelmanagement.service.dto;

import de.oth.muskelmanagement.model.EquipmentCategory;
import de.oth.muskelmanagement.model.EquipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class EquipmentDto {

    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Serial number cannot be blank")
    private String serialNumber;

    @NotNull(message = "Status cannot be null")
    private EquipmentStatus status;

    private String location;

    private LocalDate purchaseDate;

    private String manufacturer;

    private EquipmentCategory category;

    private Integer maintenanceInterval;

    private LocalDate lastMaintenanceDate;

    private boolean archived;

    public EquipmentDto() {
    }

    public EquipmentDto(Long id, String name, String serialNumber, EquipmentStatus status, String location,
                        LocalDate purchaseDate, String manufacturer, EquipmentCategory category,
                        Integer maintenanceInterval, LocalDate lastMaintenanceDate, boolean archived) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.status = status;
        this.location = location;
        this.purchaseDate = purchaseDate;
        this.manufacturer = manufacturer;
        this.category = category;
        this.maintenanceInterval = maintenanceInterval;
        this.lastMaintenanceDate = lastMaintenanceDate;
        this.archived = archived;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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