package de.oth.muskelmanagement.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExerciseDto {
    private Long id;
    private String externalId;
    private String name;
    private String gifUrl;
    private String bodyPart;
    private String equipment;
    private String target;
    private List<String> secondaryMuscles = new ArrayList<>();
    private List<String> instructions = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime lastSyncedAt;

    public ExerciseDto() {
    }

    public ExerciseDto(Long id, String externalId, String name, String gifUrl, String bodyPart, String equipment, String target, List<String> secondaryMuscles, List<String> instructions, LocalDateTime createdAt, LocalDateTime lastSyncedAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.gifUrl = gifUrl;
        this.bodyPart = bodyPart;
        this.equipment = equipment;
        this.target = target;
        this.secondaryMuscles = secondaryMuscles;
        this.instructions = instructions;
        this.createdAt = createdAt;
        this.lastSyncedAt = lastSyncedAt;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public String getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(String bodyPart) {
        this.bodyPart = bodyPart;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getSecondaryMuscles() {
        return secondaryMuscles;
    }

    public void setSecondaryMuscles(List<String> secondaryMuscles) {
        this.secondaryMuscles = secondaryMuscles;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
