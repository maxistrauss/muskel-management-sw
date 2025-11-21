package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.Equipment;
import de.oth.muskelmanagement.model.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {
    Equipment findBySerialNumber(String serialNumber);

    List<Equipment> findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrManufacturerContainingIgnoreCase(
            String name, String serialNumber, String manufacturer);

    List<Equipment> findByArchivedFalse();

    List<Equipment> findByStatus(EquipmentStatus status);
}