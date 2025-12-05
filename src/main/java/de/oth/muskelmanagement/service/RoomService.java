package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.RoomDto;
import de.oth.muskelmanagement.model.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomService {
    
    Room save(RoomDto roomDto);
    
    Room findEntityById(Long id);
    
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
