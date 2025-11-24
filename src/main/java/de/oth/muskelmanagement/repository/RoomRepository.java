package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    
    Optional<Room> findByName(String name);
    
    List<Room> findByActiveTrue();
    
    List<Room> findByActiveFalse();
    
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
    
    List<Room> findByCapacityLessThanEqual(Integer capacity);
    
    List<Room> findByNameContainingIgnoreCase(String name);
}