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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceImplTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private EquipmentDto equipmentDto;
    private Equipment equipment;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);

        equipmentDto = new EquipmentDto();
        equipmentDto.setId(1L);
        equipmentDto.setName("Treadmill");
        equipmentDto.setSerialNumber("TM-2024-001");
        equipmentDto.setStatus(EquipmentStatus.AVAILABLE);
        equipmentDto.setLocation("Cardio Area");
        equipmentDto.setPurchaseDate(LocalDate.of(2024, 1, 15));
        equipmentDto.setManufacturer("Life Fitness");
        equipmentDto.setCategory(EquipmentCategory.CARDIO);
        equipmentDto.setMaintenanceInterval(90);
        equipmentDto.setLastMaintenanceDate(LocalDate.of(2024, 10, 1));
        equipmentDto.setArchived(false);

        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Treadmill");
        equipment.setSerialNumber("TM-2024-001");
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setLocation("Cardio Area");
        equipment.setPurchaseDate(LocalDate.of(2024, 1, 15));
        equipment.setManufacturer("Life Fitness");
        equipment.setCategory(EquipmentCategory.CARDIO);
        equipment.setMaintenanceInterval(90);
        equipment.setLastMaintenanceDate(LocalDate.of(2024, 10, 1));
        equipment.setArchived(false);
    }

    @Test
    void save_shouldCreateEquipmentWithAllFields() {
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> {
            Equipment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        Equipment savedEquipment = equipmentService.save(equipmentDto);

        assertNotNull(savedEquipment);
        assertEquals("Treadmill", savedEquipment.getName());
        assertEquals("TM-2024-001", savedEquipment.getSerialNumber());
        assertEquals(EquipmentStatus.AVAILABLE, savedEquipment.getStatus());
        assertEquals("Cardio Area", savedEquipment.getLocation());
        assertEquals(LocalDate.of(2024, 1, 15), savedEquipment.getPurchaseDate());
        assertEquals("Life Fitness", savedEquipment.getManufacturer());
        assertEquals(EquipmentCategory.CARDIO, savedEquipment.getCategory());
        assertEquals(90, savedEquipment.getMaintenanceInterval());
        assertEquals(LocalDate.of(2024, 10, 1), savedEquipment.getLastMaintenanceDate());
        assertFalse(savedEquipment.isArchived());
        
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
    }

    @Test
    void save_shouldHandleMinimalEquipmentData() {
        EquipmentDto minimalDto = new EquipmentDto();
        minimalDto.setName("Basic Equipment");
        minimalDto.setSerialNumber("BE-001");
        minimalDto.setStatus(EquipmentStatus.AVAILABLE);

        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> {
            Equipment saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        Equipment savedEquipment = equipmentService.save(minimalDto);

        assertNotNull(savedEquipment);
        assertEquals("Basic Equipment", savedEquipment.getName());
        assertEquals("BE-001", savedEquipment.getSerialNumber());
        assertEquals(EquipmentStatus.AVAILABLE, savedEquipment.getStatus());
        assertNull(savedEquipment.getLocation());
        assertNull(savedEquipment.getManufacturer());
        verify(equipmentRepository, times(1)).save(any(Equipment.class));
    }

    @Test
    void findBySerialNumber_shouldReturnEquipmentWhenExists() {
        when(equipmentRepository.findBySerialNumber("TM-2024-001")).thenReturn(equipment);

        Equipment foundEquipment = equipmentService.findBySerialNumber("TM-2024-001");

        assertNotNull(foundEquipment);
        assertEquals("TM-2024-001", foundEquipment.getSerialNumber());
        assertEquals("Treadmill", foundEquipment.getName());
        verify(equipmentRepository, times(1)).findBySerialNumber("TM-2024-001");
    }

    @Test
    void findBySerialNumber_shouldReturnNullWhenNotExists() {
        when(equipmentRepository.findBySerialNumber("NONEXISTENT")).thenReturn(null);

        Equipment foundEquipment = equipmentService.findBySerialNumber("NONEXISTENT");

        assertNull(foundEquipment);
        verify(equipmentRepository, times(1)).findBySerialNumber("NONEXISTENT");
    }

    @Test
    void findAll_shouldReturnPagedEquipment() {
        List<Equipment> equipmentList = Arrays.asList(equipment, createSecondEquipment());
        Page<Equipment> equipmentPage = new PageImpl<>(equipmentList, pageable, equipmentList.size());
        
        when(equipmentRepository.findAll(pageable)).thenReturn(equipmentPage);

        Page<EquipmentDto> result = equipmentService.findAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Treadmill", result.getContent().get(0).getName());
        assertEquals("Weight Bench", result.getContent().get(1).getName());
        verify(equipmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void findAll_shouldReturnEmptyPageWhenNoEquipment() {
        Page<Equipment> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(equipmentRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<EquipmentDto> result = equipmentService.findAll(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(equipmentRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_shouldReturnEquipmentDtoWhenExists() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        EquipmentDto result = equipmentService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Treadmill", result.getName());
        assertEquals("TM-2024-001", result.getSerialNumber());
        verify(equipmentRepository, times(1)).findById(1L);
    }

    @Test
    void findById_shouldThrowExceptionWhenNotExists() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.findById(999L);
        });

        assertEquals("Equipment not found", exception.getMessage());
        verify(equipmentRepository, times(1)).findById(999L);
    }

    @Test
    void updateEquipment_shouldUpdateAllFields() {
        Equipment existingEquipment = new Equipment();
        existingEquipment.setId(1L);
        existingEquipment.setName("Old Name");
        existingEquipment.setSerialNumber("OLD-001");
        existingEquipment.setStatus(EquipmentStatus.AVAILABLE);

        EquipmentDto updateDto = new EquipmentDto();
        updateDto.setId(1L);
        updateDto.setName("Updated Treadmill");
        updateDto.setSerialNumber("TM-2024-002");
        updateDto.setStatus(EquipmentStatus.AVAILABLE);
        updateDto.setLocation("New Location");
        updateDto.setPurchaseDate(LocalDate.of(2024, 6, 1));
        updateDto.setManufacturer("New Manufacturer");
        updateDto.setCategory(EquipmentCategory.STRENGTH);
        updateDto.setMaintenanceInterval(60);
        updateDto.setLastMaintenanceDate(LocalDate.of(2024, 11, 1));
        updateDto.setArchived(true);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(existingEquipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        equipmentService.updateEquipment(updateDto);

        assertEquals("Updated Treadmill", existingEquipment.getName());
        assertEquals("TM-2024-002", existingEquipment.getSerialNumber());
        assertEquals(EquipmentStatus.AVAILABLE, existingEquipment.getStatus());
        assertEquals("New Location", existingEquipment.getLocation());
        assertEquals(LocalDate.of(2024, 6, 1), existingEquipment.getPurchaseDate());
        assertEquals("New Manufacturer", existingEquipment.getManufacturer());
        assertEquals(EquipmentCategory.STRENGTH, existingEquipment.getCategory());
        assertEquals(60, existingEquipment.getMaintenanceInterval());
        assertEquals(LocalDate.of(2024, 11, 1), existingEquipment.getLastMaintenanceDate());
        assertTrue(existingEquipment.isArchived());
        
        verify(equipmentRepository, times(1)).save(existingEquipment);
    }

    @Test
    void updateEquipment_shouldThrowExceptionWhenNotExists() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        EquipmentDto updateDto = new EquipmentDto();
        updateDto.setId(999L);
        updateDto.setName("Test");
        updateDto.setSerialNumber("TEST-001");
        updateDto.setStatus(EquipmentStatus.AVAILABLE);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.updateEquipment(updateDto);
        });

        assertEquals("Equipment not found", exception.getMessage());
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void archiveEquipment_shouldSetArchivedTrueAndStatusArchived() {
        Equipment equipmentToArchive = new Equipment();
        equipmentToArchive.setId(1L);
        equipmentToArchive.setName("Treadmill");
        equipmentToArchive.setSerialNumber("TM-001");
        equipmentToArchive.setStatus(EquipmentStatus.AVAILABLE);
        equipmentToArchive.setArchived(false);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipmentToArchive));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        equipmentService.archiveEquipment(1L);

        assertTrue(equipmentToArchive.isArchived());
        assertEquals(EquipmentStatus.ARCHIVED, equipmentToArchive.getStatus());
        verify(equipmentRepository, times(1)).save(equipmentToArchive);
    }

    @Test
    void archiveEquipment_shouldThrowExceptionWhenNotExists() {
        when(equipmentRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            equipmentService.archiveEquipment(999L);
        });

        assertEquals("Equipment not found", exception.getMessage());
        verify(equipmentRepository, never()).save(any(Equipment.class));
    }

    @Test
    void deleteEquipment_shouldDeleteEquipment() {
        doNothing().when(equipmentRepository).deleteById(1L);

        equipmentService.deleteEquipment(1L);

        verify(equipmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void findEquipment_shouldFilterByName() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment("Treadmill", null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Treadmill", result.getContent().get(0).getName());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldFilterBySerialNumber() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment(null, "TM-2024", null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldFilterByManufacturer() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment(null, null, "Life Fitness", null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldFilterByStatus() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment(null, null, null, EquipmentStatus.AVAILABLE, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(EquipmentStatus.AVAILABLE, result.getContent().get(0).getStatus());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldFilterByLocation() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment(null, null, null, null, "Cardio", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldFilterByMultipleCriteria() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment(
            "Treadmill", 
            "TM-2024", 
            "Life Fitness", 
            EquipmentStatus.AVAILABLE, 
            "Cardio", 
            pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldReturnAllWhenNoFilterProvided() {
        List<Equipment> allEquipment = Arrays.asList(equipment, createSecondEquipment());
        Page<Equipment> allEquipmentPage = new PageImpl<>(allEquipment, pageable, allEquipment.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(allEquipmentPage);

        Page<EquipmentDto> result = equipmentService.findEquipment(null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void findEquipment_shouldHandleCaseInsensitiveSearch() {
        List<Equipment> filteredList = Arrays.asList(equipment);
        Page<Equipment> filteredPage = new PageImpl<>(filteredList, pageable, filteredList.size());
        
        when(equipmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(filteredPage);

        Page<EquipmentDto> result = equipmentService.findEquipment("TREADMILL", null, null, null, null, pageable);

        assertNotNull(result);
        verify(equipmentRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    // Helper method to create a second equipment for testing
    private Equipment createSecondEquipment() {
        Equipment second = new Equipment();
        second.setId(2L);
        second.setName("Weight Bench");
        second.setSerialNumber("WB-2024-001");
        second.setStatus(EquipmentStatus.AVAILABLE);
        second.setLocation("Strength Area");
        second.setPurchaseDate(LocalDate.of(2024, 2, 10));
        second.setManufacturer("Rogue Fitness");
        second.setCategory(EquipmentCategory.STRENGTH);
        second.setMaintenanceInterval(180);
        second.setLastMaintenanceDate(LocalDate.of(2024, 9, 1));
        second.setArchived(false);
        return second;
    }
}