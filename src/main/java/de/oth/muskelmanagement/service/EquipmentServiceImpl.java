package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Equipment;
import de.oth.muskelmanagement.model.EquipmentStatus;
import de.oth.muskelmanagement.model.Room;
import de.oth.muskelmanagement.repository.EquipmentRepository;
import de.oth.muskelmanagement.repository.RoomRepository;
import de.oth.muskelmanagement.service.dto.EquipmentDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RoomRepository roomRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository, RoomRepository roomRepository) {
        this.equipmentRepository = equipmentRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public Equipment save(EquipmentDto equipmentDto) {
        Equipment equipment = new Equipment();
        equipment.setName(equipmentDto.getName());
        equipment.setSerialNumber(equipmentDto.getSerialNumber());
        equipment.setStatus(equipmentDto.getStatus());
        
        // Handle room assignment
        if (equipmentDto.getRoomId() != null) {
            Room room = roomRepository.findById(equipmentDto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + equipmentDto.getRoomId()));
            equipment.setRoom(room);
        }
        
        equipment.setPurchaseDate(equipmentDto.getPurchaseDate());
        equipment.setManufacturer(equipmentDto.getManufacturer());
        equipment.setCategory(equipmentDto.getCategory());
        equipment.setMaintenanceInterval(equipmentDto.getMaintenanceInterval());
        equipment.setLastMaintenanceDate(equipmentDto.getLastMaintenanceDate());
        equipment.setArchived(equipmentDto.isArchived());

        return equipmentRepository.save(equipment);
    }

    @Override
    public Equipment findBySerialNumber(String serialNumber) {
        return equipmentRepository.findBySerialNumber(serialNumber);
    }

    @Override
    public Page<EquipmentDto> findAll(Pageable pageable) {
        return equipmentRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public EquipmentDto findById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        return convertToDto(equipment);
    }

    @Override
    public void updateEquipment(EquipmentDto equipmentDto) {
        Equipment equipment = equipmentRepository.findById(equipmentDto.getId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        
        equipment.setName(equipmentDto.getName());
        equipment.setSerialNumber(equipmentDto.getSerialNumber());
        equipment.setStatus(equipmentDto.getStatus());
        
        // Handle room assignment
        if (equipmentDto.getRoomId() != null) {
            Room room = roomRepository.findById(equipmentDto.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room not found with id: " + equipmentDto.getRoomId()));
            equipment.setRoom(room);
        } else {
            equipment.setRoom(null);
        }
        
        equipment.setPurchaseDate(equipmentDto.getPurchaseDate());
        equipment.setManufacturer(equipmentDto.getManufacturer());
        equipment.setCategory(equipmentDto.getCategory());
        equipment.setMaintenanceInterval(equipmentDto.getMaintenanceInterval());
        equipment.setLastMaintenanceDate(equipmentDto.getLastMaintenanceDate());
        equipment.setArchived(equipmentDto.isArchived());

        equipmentRepository.save(equipment);
    }

    @Override
    public void archiveEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        equipment.setArchived(true);
        equipment.setStatus(EquipmentStatus.ARCHIVED);
        equipmentRepository.save(equipment);
    }

    @Override
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Override
    public Page<EquipmentDto> findEquipment(String name, String serialNumber, String manufacturer,
                                             EquipmentStatus status, String location, Pageable pageable) {
        Specification<Equipment> spec = (Root<Equipment> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(serialNumber)) {
                predicates.add(cb.like(cb.lower(root.get("serialNumber")), "%" + serialNumber.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(manufacturer)) {
                predicates.add(cb.like(cb.lower(root.get("manufacturer")), "%" + manufacturer.toLowerCase() + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(location)) {
                // Search by room name
                predicates.add(cb.like(cb.lower(root.get("room").get("name")), "%" + location.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return equipmentRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    private EquipmentDto convertToDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setStatus(equipment.getStatus());
        
        // Handle room mapping
        if (equipment.getRoom() != null) {
            dto.setRoomId(equipment.getRoom().getId());
            dto.setRoomName(equipment.getRoom().getName());
        }
        
        dto.setPurchaseDate(equipment.getPurchaseDate());
        dto.setManufacturer(equipment.getManufacturer());
        dto.setCategory(equipment.getCategory());
        dto.setMaintenanceInterval(equipment.getMaintenanceInterval());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setArchived(equipment.isArchived());
        return dto;
    }
}