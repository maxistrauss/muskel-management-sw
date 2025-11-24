package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Equipment;
import de.oth.muskelmanagement.model.Room;
import de.oth.muskelmanagement.repository.EquipmentRepository;
import de.oth.muskelmanagement.repository.RoomRepository;
import de.oth.muskelmanagement.service.dto.EquipmentDto;
import de.oth.muskelmanagement.service.dto.RoomDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;

    public RoomServiceImpl(RoomRepository roomRepository, EquipmentRepository equipmentRepository) {
        this.roomRepository = roomRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    @Transactional
    public Room save(RoomDto roomDto) {
        Room room = new Room();
        room.setName(roomDto.getName());
        room.setCapacity(roomDto.getCapacity());
        room.setAmenities(roomDto.getAmenities());
        room.setActive(roomDto.isActive());

        return roomRepository.save(room);
    }

    @Override
    public Page<RoomDto> findAll(Pageable pageable) {
        return roomRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public RoomDto findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        return convertToDtoWithEquipment(room);
    }

    @Override
    @Transactional
    public void update(RoomDto roomDto) {
        Room room = roomRepository.findById(roomDto.getId())
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + roomDto.getId()));
        
        room.setName(roomDto.getName());
        room.setCapacity(roomDto.getCapacity());
        room.setAmenities(roomDto.getAmenities());
        room.setActive(roomDto.isActive());

        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
        
        // Unassign all equipment from this room
        unassignEquipmentFromRoom(id);
        
        // Deactivate the room
        room.setActive(false);
        roomRepository.save(room);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // First unassign all equipment from this room
        unassignEquipmentFromRoom(id);
        
        // Then delete the room
        roomRepository.deleteById(id);
    }

    @Override
    public Page<RoomDto> searchRooms(String name, Integer minCapacity, Integer maxCapacity, 
                                     Boolean active, Pageable pageable) {
        Specification<Room> spec = (Root<Room> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (minCapacity != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), minCapacity));
            }
            if (maxCapacity != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("capacity"), maxCapacity));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return roomRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    @Override
    public List<RoomDto> findActiveRooms() {
        return roomRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void unassignEquipmentFromRoom(Long roomId) {
        List<Equipment> equipmentList = equipmentRepository.findByRoomId(roomId);
        for (Equipment equipment : equipmentList) {
            equipment.setRoom(null);
            equipmentRepository.save(equipment);
        }
    }

    private RoomDto convertToDto(Room room) {
        RoomDto dto = new RoomDto();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCapacity(room.getCapacity());
        dto.setAmenities(room.getAmenities());
        dto.setActive(room.isActive());
        dto.setEquipmentCount(room.getEquipment() != null ? room.getEquipment().size() : 0);
        return dto;
    }

    private RoomDto convertToDtoWithEquipment(Room room) {
        RoomDto dto = convertToDto(room);
        
        // Load equipment for this room
        List<Equipment> equipmentList = equipmentRepository.findByRoomId(room.getId());
        List<EquipmentDto> equipmentDtos = equipmentList.stream()
                .map(this::convertEquipmentToDto)
                .collect(Collectors.toList());
        
        dto.setEquipment(equipmentDtos);
        dto.setEquipmentCount(equipmentDtos.size());
        
        return dto;
    }

    private EquipmentDto convertEquipmentToDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setStatus(equipment.getStatus());
        dto.setRoomId(equipment.getRoom() != null ? equipment.getRoom().getId() : null);
        dto.setRoomName(equipment.getRoom() != null ? equipment.getRoom().getName() : null);
        dto.setPurchaseDate(equipment.getPurchaseDate());
        dto.setManufacturer(equipment.getManufacturer());
        dto.setCategory(equipment.getCategory());
        dto.setMaintenanceInterval(equipment.getMaintenanceInterval());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setArchived(equipment.isArchived());
        return dto;
    }
}