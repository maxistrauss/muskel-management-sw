package de.oth.muskelmanagement.controller.api;

import de.oth.muskelmanagement.dto.EquipmentDto;
import de.oth.muskelmanagement.model.enums.EquipmentStatus;
import de.oth.muskelmanagement.service.EquipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/equipment")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEquipmentRestController {

    private final EquipmentService equipmentService;

    public AdminEquipmentRestController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping
    public ResponseEntity<Page<EquipmentDto>> findEquipment(@RequestParam(required = false) String name,
            @RequestParam(required = false) String serialNumber, @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) EquipmentStatus status, @RequestParam(required = false) String location,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<EquipmentDto> equipment = equipmentService.findEquipment(name, serialNumber, manufacturer, status,
                location, pageable);
        return ResponseEntity.ok(equipment);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDto> getEquipmentById(@PathVariable Long id) {
        EquipmentDto equipment = equipmentService.findById(id);
        return ResponseEntity.ok(equipment);
    }

    @PostMapping
    public ResponseEntity<EquipmentDto> createEquipment(@Valid @RequestBody EquipmentDto equipmentDto) {
        equipmentService.save(equipmentDto);
        return new ResponseEntity<>(equipmentDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDto> updateEquipment(@PathVariable Long id,
            @Valid @RequestBody EquipmentDto equipmentDto) {
        equipmentDto.setId(id);
        equipmentService.updateEquipment(equipmentDto);
        return ResponseEntity.ok(equipmentDto);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archiveEquipment(@PathVariable Long id) {
        equipmentService.archiveEquipment(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
