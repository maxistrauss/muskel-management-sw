package de.oth.muskelmanagement.model.entity;

import de.oth.muskelmanagement.model.enums.TrainingPlanStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "training_plans")
public class TrainingPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Enumerated(EnumType.STRING)
    private TrainingPlanStatus status = TrainingPlanStatus.ACTIVE;

    private int version = 1;

    private LocalDateTime creationDate;
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "trainingPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<TrainingPlanExercise> exercises = new ArrayList<>();

    public TrainingPlan() {
        this.creationDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public TrainingPlan(String name, String description, User member, User trainer) {
        this();
        this.name = name;
        this.description = description;
        this.member = member;
        this.trainer = trainer;
    }

    @PreUpdate
    public void onUpdate() {
        this.lastModifiedDate = LocalDateTime.now();
    }

    // Getters and Setters

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

    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
    }

    public User getTrainer() {
        return trainer;
    }

    public void setTrainer(User trainer) {
        this.trainer = trainer;
    }

    public TrainingPlanStatus getStatus() {
        return status;
    }

    public void setStatus(TrainingPlanStatus status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<TrainingPlanExercise> getExercises() {
        return exercises;
    }

    public void setExercises(List<TrainingPlanExercise> exercises) {
        this.exercises = exercises;
    }

    public void addExercise(TrainingPlanExercise exercise) {
        exercises.add(exercise);
        exercise.setTrainingPlan(this);
    }

    public void removeExercise(TrainingPlanExercise exercise) {
        exercises.remove(exercise);
        exercise.setTrainingPlan(null);
    }
}
