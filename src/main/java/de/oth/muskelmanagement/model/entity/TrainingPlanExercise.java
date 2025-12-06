package de.oth.muskelmanagement.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "training_plan_exercises")
public class TrainingPlanExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "training_plan_id", nullable = false)
    private TrainingPlan trainingPlan;

    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private int sets;
    private String reps; // String to allow ranges like "8-12"

    @Column(columnDefinition = "TEXT")
    private String notes;

    private int orderIndex;

    public TrainingPlanExercise() {
    }

    public TrainingPlanExercise(Exercise exercise, int sets, String reps, String notes, int orderIndex) {
        this.exercise = exercise;
        this.sets = sets;
        this.reps = reps;
        this.notes = notes;
        this.orderIndex = orderIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TrainingPlan getTrainingPlan() {
        return trainingPlan;
    }

    public void setTrainingPlan(TrainingPlan trainingPlan) {
        this.trainingPlan = trainingPlan;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public String getReps() {
        return reps;
    }

    public void setReps(String reps) {
        this.reps = reps;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
