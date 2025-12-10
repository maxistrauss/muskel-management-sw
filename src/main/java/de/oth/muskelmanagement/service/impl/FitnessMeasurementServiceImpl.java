package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.FitnessMeasurementDto;
import de.oth.muskelmanagement.model.entity.FitnessMeasurement;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.FitnessMeasurementRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.FitnessMeasurementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FitnessMeasurementServiceImpl implements FitnessMeasurementService {

    private final FitnessMeasurementRepository fitnessMeasurementRepository;
    private final UserRepository userRepository;

    public FitnessMeasurementServiceImpl(FitnessMeasurementRepository fitnessMeasurementRepository,
            UserRepository userRepository) {
        this.fitnessMeasurementRepository = fitnessMeasurementRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public FitnessMeasurement createMeasurement(FitnessMeasurementDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        validateMeasurementValues(dto);

        FitnessMeasurement measurement = new FitnessMeasurement();
        measurement.setUser(user);
        measurement.setDate(dto.getDate());
        measurement.setWeight(dto.getWeight());
        measurement.setBodyFatPercentage(dto.getBodyFatPercentage());
        measurement.setMuscleMassPercentage(dto.getMuscleMassPercentage());
        measurement.setNotes(dto.getNotes());

        return fitnessMeasurementRepository.save(measurement);
    }

    @Override
    @Transactional
    public FitnessMeasurement updateMeasurement(Long id, FitnessMeasurementDto dto) {
        FitnessMeasurement measurement = getMeasurementById(id);

        validateMeasurementValues(dto);

        measurement.setDate(dto.getDate());
        measurement.setWeight(dto.getWeight());
        measurement.setBodyFatPercentage(dto.getBodyFatPercentage());
        measurement.setMuscleMassPercentage(dto.getMuscleMassPercentage());
        measurement.setNotes(dto.getNotes());

        return fitnessMeasurementRepository.save(measurement);
    }

    private void validateMeasurementValues(FitnessMeasurementDto dto) {
        if (dto.getWeight() != null && dto.getWeight() < 0) {
            throw new IllegalArgumentException("Weight cannot be negative.");
        }
        if (dto.getBodyFatPercentage() != null && dto.getBodyFatPercentage() < 0) {
            throw new IllegalArgumentException("Body fat percentage cannot be negative.");
        }
        if (dto.getMuscleMassPercentage() != null && dto.getMuscleMassPercentage() < 0) {
            throw new IllegalArgumentException("Muscle mass percentage cannot be negative.");
        }
    }

    @Override
    @Transactional
    public void deleteMeasurement(Long id) {
        fitnessMeasurementRepository.deleteById(id);
    }

    @Override
    public FitnessMeasurement getMeasurementById(Long id) {
        return fitnessMeasurementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Measurement not found"));
    }

    @Override
    public List<FitnessMeasurement> getMeasurementsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return fitnessMeasurementRepository.findByUserOrderByDateDesc(user);
    }

    @Override
    public FitnessMeasurementDto toDto(FitnessMeasurement measurement) {
        FitnessMeasurementDto dto = new FitnessMeasurementDto();
        dto.setId(measurement.getId());
        dto.setUserId(measurement.getUser().getId());
        dto.setDate(measurement.getDate());
        dto.setWeight(measurement.getWeight());
        dto.setBodyFatPercentage(measurement.getBodyFatPercentage());
        dto.setMuscleMassPercentage(measurement.getMuscleMassPercentage());
        dto.setNotes(measurement.getNotes());
        return dto;
    }
}
