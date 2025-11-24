package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Room;
import de.oth.muskelmanagement.service.dto.RoomDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomService {
    
    Room save(RoomDto roomDto);
    
    Page<RoomDto> findAll(Pageable pageable);
    
    RoomDto findById(Long id);
    
    void update(RoomDto roomDto);
    
    void deactivate(Long id);
    
    void delete(Long id);
    
    Page<RoomDto> searchRooms(String name, Integer minCapacity, Integer maxCapacity, 
                              Boolean active, Pageable pageable);
    
    List<RoomDto> findActiveRooms();
    
    void unassignEquipmentFromRoom(Long roomId);
}