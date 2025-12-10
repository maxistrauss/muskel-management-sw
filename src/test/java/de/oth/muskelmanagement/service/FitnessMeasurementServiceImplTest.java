package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.FitnessMeasurementDto;
import de.oth.muskelmanagement.model.entity.FitnessMeasurement;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.FitnessMeasurementRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.impl.FitnessMeasurementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FitnessMeasurementServiceImplTest {

    @Mock
    private FitnessMeasurementRepository fitnessMeasurementRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FitnessMeasurementServiceImpl fitnessMeasurementService;

    private User user;
    private FitnessMeasurementDto dto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("member@example.com");

        dto = new FitnessMeasurementDto();
        dto.setUserId(1L);
        dto.setDate(LocalDate.now());
        dto.setWeight(80.0);
        dto.setBodyFatPercentage(15.0);
        dto.setMuscleMassPercentage(40.0);
        dto.setNotes("Test Note");
    }

    @Test
    void createMeasurement_ShouldCreate_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fitnessMeasurementRepository.save(any(FitnessMeasurement.class))).thenAnswer(inv -> inv.getArgument(0));

        FitnessMeasurement created = fitnessMeasurementService.createMeasurement(dto);

        assertNotNull(created);
        assertEquals(80.0, created.getWeight());
        assertEquals(user, created.getUser());
        verify(fitnessMeasurementRepository).save(any(FitnessMeasurement.class));
    }

    @Test
    void createMeasurement_ShouldThrow_WhenNegativeValues() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        dto.setWeight(-5.0);
        assertThrows(IllegalArgumentException.class, () -> fitnessMeasurementService.createMeasurement(dto));

        dto.setWeight(80.0);
        dto.setBodyFatPercentage(-1.0);
        assertThrows(IllegalArgumentException.class, () -> fitnessMeasurementService.createMeasurement(dto));
    }

    @Test
    void getMeasurementsByUser_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fitnessMeasurementRepository.findByUserOrderByDateDesc(user)).thenReturn(
                List.of(new FitnessMeasurement(), new FitnessMeasurement()));

        List<FitnessMeasurement> result = fitnessMeasurementService.getMeasurementsByUser(1L);

        assertEquals(2, result.size());
        verify(fitnessMeasurementRepository).findByUserOrderByDateDesc(user);
    }

    @Test
    void updateMeasurement_ShouldUpdateValues() {
        FitnessMeasurement existing = new FitnessMeasurement();
        existing.setId(1L);
        existing.setWeight(70.0);

        when(fitnessMeasurementRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(fitnessMeasurementRepository.save(any(FitnessMeasurement.class))).thenAnswer(inv -> inv.getArgument(0));

        dto.setWeight(75.0);
        FitnessMeasurement updated = fitnessMeasurementService.updateMeasurement(1L, dto);

        assertEquals(75.0, updated.getWeight());
    }
}
