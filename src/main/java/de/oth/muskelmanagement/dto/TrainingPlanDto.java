package de.oth.muskelmanagement.dto;

import java.util.ArrayList;
import java.util.List;

public class TrainingPlanDto {
    private Long id;
    private String name;
    private String description;
    private Long memberId;
    private List<TrainingPlanExerciseDto> exercises = new ArrayList<>();

    public TrainingPlanDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public List<TrainingPlanExerciseDto> getExercises() {
        return exercises;
    }

    public void setExercises(List<TrainingPlanExerciseDto> exercises) {
        this.exercises = exercises;
    }
}
