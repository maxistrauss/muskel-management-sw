package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Equipment;
import de.oth.muskelmanagement.model.EquipmentStatus;
import de.oth.muskelmanagement.service.dto.EquipmentDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentService {
    Equipment save(EquipmentDto equipmentDto);

    Equipment findBySerialNumber(String serialNumber);

    Page<EquipmentDto> findAll(Pageable pageable);

    EquipmentDto findById(Long id);

    void updateEquipment(EquipmentDto equipmentDto);

    void archiveEquipment(Long id);

    void deleteEquipment(Long id);

    Page<EquipmentDto> findEquipment(String name, String serialNumber, String manufacturer, 
                                      EquipmentStatus status, String location, Pageable pageable);
}