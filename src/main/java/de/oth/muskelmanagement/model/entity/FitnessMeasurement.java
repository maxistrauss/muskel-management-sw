package de.oth.muskelmanagement.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "fitness_measurements")
public class FitnessMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    private Double weight; // in kg

    private Double bodyFatPercentage;

    private Double muscleMassPercentage;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public FitnessMeasurement() {
    }

    public FitnessMeasurement(User user, LocalDate date, Double weight, Double bodyFatPercentage,
            Double muscleMassPercentage, String notes) {
        this.user = user;
        this.date = date;
        this.weight = weight;
        this.bodyFatPercentage = bodyFatPercentage;
        this.muscleMassPercentage = muscleMassPercentage;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(Double bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public Double getMuscleMassPercentage() {
        return muscleMassPercentage;
    }

    public void setMuscleMassPercentage(Double muscleMassPercentage) {
        this.muscleMassPercentage = muscleMassPercentage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
