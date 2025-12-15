package de.oth.muskelmanagement.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Column(columnDefinition = "TEXT")
    private String amenities; // Built-in features: "Mirrors, Air conditioning, Sound system"

    @Column(nullable = false)
    private boolean active = true; // Soft delete flag

    @OneToMany(mappedBy = "room", cascade = CascadeType.PERSIST, orphanRemoval = false)
    private List<Equipment> equipment = new ArrayList<>();

    public Room() {
    }

    public Room(String name, Integer capacity, String amenities, boolean active) {
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

    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    public void addEquipment(Equipment equipmentItem) {
        equipment.add(equipmentItem);
        equipmentItem.setRoom(this);
    }

    public void removeEquipment(Equipment equipmentItem) {
        equipment.remove(equipmentItem);
        equipmentItem.setRoom(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Room room = (Room) o;
        return id != null && id.equals(room.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
