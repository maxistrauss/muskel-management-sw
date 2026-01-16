package de.oth.muskelmanagement.controller.api;

import de.oth.muskelmanagement.dto.RoomDto;
import de.oth.muskelmanagement.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomRestController {

    private final RoomService roomService;

    public AdminRoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomDto> createRoom(@Valid @RequestBody RoomDto roomDto) {
        roomService.save(roomDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(roomDto);
    }

    @GetMapping
    public ResponseEntity<Page<RoomDto>> getAllRooms(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) Boolean active) {
        
        Page<RoomDto> roomPage = roomService.searchRooms(name, minCapacity, maxCapacity, active, pageable);
        return ResponseEntity.ok(roomPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        try {
            RoomDto room = roomService.findById(id);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDto> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomDto roomDto) {
        try {
            roomDto.setId(id); // Ensure the ID from the path is used
            roomService.update(roomDto);
            return ResponseEntity.ok(roomDto);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        try {
            roomService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) { // Catching for robustness, though RoomService handles dependencies
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateRoom(@PathVariable Long id) {
        try {
            roomService.deactivate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
