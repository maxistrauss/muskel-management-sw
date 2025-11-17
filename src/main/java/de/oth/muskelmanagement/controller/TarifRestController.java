package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.service.TarifService;
import de.oth.muskelmanagement.service.dto.TarifDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tarifs")
@PreAuthorize("hasRole('ADMIN')")
public class TarifRestController {

    private final TarifService tarifService;

    public TarifRestController(TarifService tarifService) {
        this.tarifService = tarifService;
    }

    @GetMapping
    public ResponseEntity<Page<TarifDto>> findTarifs(@RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active, @PageableDefault(size = 10) Pageable pageable) {
        Page<TarifDto> tarifs = tarifService.findTarifs(name, active, pageable);
        return ResponseEntity.ok(tarifs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifDto> getTarifById(@PathVariable Long id) {
        TarifDto tarif = tarifService.findById(id);
        return ResponseEntity.ok(tarif);
    }

    @PostMapping
    public ResponseEntity<TarifDto> createTarif(@Valid @RequestBody TarifDto tarifDto) {
        tarifService.save(tarifDto);
        return new ResponseEntity<>(tarifDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifDto> updateTarif(@PathVariable Long id, @Valid @RequestBody TarifDto tarifDto) {
        tarifDto.setId(id);
        tarifService.updateTarif(tarifDto);
        return ResponseEntity.ok(tarifDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarif(@PathVariable Long id) {
        tarifService.deleteTarif(id);
        return ResponseEntity.noContent().build();
    }
}

