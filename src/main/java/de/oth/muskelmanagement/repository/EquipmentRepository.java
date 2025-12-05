package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Equipment;
import de.oth.muskelmanagement.model.entity.Room;
import de.oth.muskelmanagement.model.enums.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {
    Equipment findBySerialNumber(String serialNumber);

    List<Equipment> findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrManufacturerContainingIgnoreCase(
            String name, String serialNumber, String manufacturer);

    List<Equipment> findByArchivedFalse();

    List<Equipment> findByStatus(EquipmentStatus status);
    
    List<Equipment> findByRoom(Room room);
    
    List<Equipment> findByRoomId(Long roomId);
    
    List<Equipment> findByRoomIsNull();
}
