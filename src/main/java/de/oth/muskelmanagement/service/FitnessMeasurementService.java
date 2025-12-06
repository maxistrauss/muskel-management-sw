package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.FitnessMeasurementDto;
import de.oth.muskelmanagement.model.entity.FitnessMeasurement;

import java.util.List;

public interface FitnessMeasurementService {
    FitnessMeasurement createMeasurement(FitnessMeasurementDto dto);

    FitnessMeasurement updateMeasurement(Long id, FitnessMeasurementDto dto);

    void deleteMeasurement(Long id);

    FitnessMeasurement getMeasurementById(Long id);

    List<FitnessMeasurement> getMeasurementsByUser(Long userId);

    FitnessMeasurementDto toDto(FitnessMeasurement measurement);
}
