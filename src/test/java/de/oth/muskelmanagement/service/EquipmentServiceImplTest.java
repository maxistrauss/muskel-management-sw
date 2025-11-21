package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Equipment;
import de.oth.muskelmanagement.model.EquipmentCategory;
import de.oth.muskelmanagement.model.EquipmentStatus;
import de.oth.muskelmanagement.repository.EquipmentRepository;
import de.oth.muskelmanagement.service.dto.EquipmentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceImplTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private EquipmentDto equipmentDto;
    private Equipment equipment;

    @BeforeEach
    void setUp() {
        equipmentDto = new EquipmentDto();
        equipmentDto.setId(1L);
        equipmentDto.setName("Laufband Pro 3000");
        equipmentDto.setSerialNumber("SN-2024-001");
        equipmentDto.setStatus(EquipmentStatus.AVAILABLE);
        equipmentDto.setLocation("Cardio-Bereich");
        equipmentDto.setPurchaseDate(LocalDate.of(2024, 1, 15));
        equipmentDto.setManufacturer("TechFit");
        equipmentDto.setCategory(EquipmentCategory.CARDIO);
        equipmentDto.setMaintenanceInterval(90);
        equipmentDto.setLastMaintenanceDate(LocalDate.of(2024, 10, 1));
        equipmentDto.setArchived(false);

        equipment = new Equipment(
                "Laufband Pro 3000",
                "SN-2024-001",
                EquipmentStatus.AVAILABLE,
                "Cardio-Bereich",
                LocalDate.of(2024, 1, 15),
                "TechFit",
                EquipmentCategory.CARDIO,
                90,
                LocalDate.of(2024, 10, 1)
        );
        equipment.setId(1L);
        equipment.setArchived(false);
    }

    @Test
    void save_shouldCreateEquipmentWithAllProperties() {
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Equipment savedEquipment = equipmentService.save(equipmentDto);

        assertNotNull(savedEquipment);
        assertEquals("Laufband Pro 3000", savedEquipment.getName());
        assertEquals("SN-2024-001", savedEquipment.getSerialNumber());
        assertEquals(EquipmentStatus.AVAILABLE, savedEquipment.getStatus());
        assertEquals("Cardio-Bereich", savedEquipment.getLocation());
        assertEquals("TechFit", savedEquipment.getManufacturer());
        assertEquals(EquipmentCategory.CARDIO, savedEquipment.getCategory());
        assertEquals(90, savedEquipment.getMaintenanceInterval());
        assertFalse(savedEquipment.isArchived());
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
    }

    @Test
    void findBySerialNumber_shouldReturnEquipmentIfExists() {
        when(equipmentRepository.findBySerialNumber("SN-2024-001")).thenReturn(equipment);

        Equipment foundEquipment = equipmentService.findBySerialNumber("SN-2024-001");

        assertNotNull(foundEquipment);
        assertEquals("SN-2024-001", foundEquipment.getSerialNumber());
        assertEquals("Laufband Pro 3000", foundEquipment.getName());
    }

    @Test
    void findBySerialNumber_shouldReturnNullIfEquipmentDoesNotExist() {
        when(equipmentRepository.findBySerialNumber("NONEXISTENT")).thenReturn(null);

        Equipment foundEquipment = equipmentService.findBySerialNumber("NONEXISTENT");

        assertNull(foundEquipment);
    }

    @Test
    void findById_shouldReturnEquipmentDtoIfExists() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        EquipmentDto foundDto = equipmentService.findById(1L);

        assertNotNull(foundDto);
        assertEquals(1L, foundDto.getId());
        assertEquals("Laufband Pro 3000", foundDto.getName());
        assertEquals("SN-2024-001", foundDto.getSerialNumber());
        assertEquals(EquipmentStatus.AVAILABLE, foundDto.getStatus());
    }

    @Test
    void findById_shouldThrowExceptionIfEquipmentDoesNotExist() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> equipmentService.findById(999L));
    }

    @Test
    void updateEquipment_shouldUpdateAllProperties() {
        Equipment existingEquipment = new Equipment(
                "Old Name",
                "SN-2024-001",
                EquipmentStatus.IN_MAINTENANCE,
                "Old Location",
                LocalDate.of(2023, 1, 1),
                "Old Manufacturer",
                EquipmentCategory.STRENGTH,
                60,
                LocalDate.of(2023, 6, 1)
        );
        existingEquipment.setId(1L);

        EquipmentDto updateDto = new EquipmentDto();
        updateDto.setId(1L);
        updateDto.setName("Updated Name");
        updateDto.setSerialNumber("SN-2024-001-NEW");
        updateDto.setStatus(EquipmentStatus.AVAILABLE);
        updateDto.setLocation("New Location");
        updateDto.setPurchaseDate(LocalDate.of(2024, 1, 1));
        updateDto.setManufacturer("New Manufacturer");
        updateDto.setCategory(EquipmentCategory.CARDIO);
        updateDto.setMaintenanceInterval(120);
        updateDto.setLastMaintenanceDate(LocalDate.of(2024, 11, 1));
        updateDto.setArchived(false);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(existingEquipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        equipmentService.updateEquipment(updateDto);

        assertEquals("Updated Name", existingEquipment.getName());
        assertEquals("SN-2024-001-NEW", existingEquipment.getSerialNumber());
        assertEquals(EquipmentStatus.AVAILABLE, existingEquipment.getStatus());
        assertEquals("New Location", existingEquipment.getLocation());
        assertEquals("New Manufacturer", existingEquipment.getManufacturer());
        assertEquals(EquipmentCategory.CARDIO, existingEquipment.getCategory());
        assertEquals(120, existingEquipment.getMaintenanceInterval());
        assertEquals(LocalDate.of(2024, 11, 1), existingEquipment.getLastMaintenanceDate());
        verify(equipmentRepository, times(1)).save(existingEquipment);
    }

    @Test
    void archiveEquipment_shouldSetArchivedTrueAndStatusToArchived() {
        Equipment activeEquipment = new Equipment(
                "Test Equipment",
                "SN-2024-001",
                EquipmentStatus.AVAILABLE,
                "Test Location",
                LocalDate.now(),
                "Test Manufacturer",
                EquipmentCategory.CARDIO,
                90,
                LocalDate.now()
        );
        activeEquipment.setId(1L);
        activeEquipment.setArchived(false);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(activeEquipment));
        
        ArgumentCaptor<Equipment> equipmentCaptor = ArgumentCaptor.forClass(Equipment.class);
        when(equipmentRepository.save(equipmentCaptor.capture())).thenAnswer(
                invocation -> invocation.getArgument(0));

        equipmentService.archiveEquipment(1L);

        Equipment savedEquipment = equipmentCaptor.getValue();
        assertTrue(savedEquipment.isArchived());
        assertEquals(EquipmentStatus.ARCHIVED, savedEquipment.getStatus());
        verify(equipmentRepository, times(1)).save(activeEquipment);
    }

    @Test
    void archiveEquipment_shouldThrowExceptionIfEquipmentDoesNotExist() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> equipmentService.archiveEquipment(999L));
    }

    @Test
    void deleteEquipment_shouldDeleteEquipment() {
        doNothing().when(equipmentRepository).deleteById(1L);

        equipmentService.deleteEquipment(1L);

        verify(equipmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void save_shouldHandleOptionalFieldsAsNull() {
        EquipmentDto minimalDto = new EquipmentDto();
        minimalDto.setName("Minimal Equipment");
        minimalDto.setSerialNumber("SN-MINIMAL");
        minimalDto.setStatus(EquipmentStatus.AVAILABLE);

        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        Equipment savedEquipment = equipmentService.save(minimalDto);

        assertNotNull(savedEquipment);
        assertEquals("Minimal Equipment", savedEquipment.getName());
        assertEquals("SN-MINIMAL", savedEquipment.getSerialNumber());
        assertNull(savedEquipment.getLocation());
        assertNull(savedEquipment.getManufacturer());
        assertNull(savedEquipment.getCategory());
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
    }

    @Test
    void updateEquipment_shouldThrowExceptionIfEquipmentDoesNotExist() {
        EquipmentDto updateDto = new EquipmentDto();
        updateDto.setId(999L);
        updateDto.setName("Test");
        updateDto.setSerialNumber("SN-999");
        updateDto.setStatus(EquipmentStatus.AVAILABLE);

        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> equipmentService.updateEquipment(updateDto));
    }
}