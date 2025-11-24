package de.oth.muskelmanagement.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class RoomDto {

    private Long id;

    @NotBlank(message = "Room name is required")
    @Size(max = 100, message = "Room name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    @Size(max = 500, message = "Amenities description must not exceed 500 characters")
    private String amenities;

    private boolean active = true;

    private List<EquipmentDto> equipment = new ArrayList<>();

    private Integer equipmentCount = 0;

    public RoomDto() {
    }

    public RoomDto(Long id, String name, Integer capacity, String amenities, boolean active) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.amenities = amenities;
        this.active = active;
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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<EquipmentDto> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<EquipmentDto> equipment) {
        this.equipment = equipment;
        this.equipmentCount = equipment != null ? equipment.size() : 0;
    }

    public Integer getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(Integer equipmentCount) {
        this.equipmentCount = equipmentCount;
    }
}