package de.oth.muskelmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class FitnessMeasurementDto {
    private Long id;
    private Long userId;

    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @Positive(message = "Weight must be positive")
    private Double weight;

    @Positive(message = "Body fat percentage must be positive")
    private Double bodyFatPercentage;

    @Positive(message = "Muscle mass percentage must be positive")
    private Double muscleMassPercentage;

    private String notes;

    public FitnessMeasurementDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
