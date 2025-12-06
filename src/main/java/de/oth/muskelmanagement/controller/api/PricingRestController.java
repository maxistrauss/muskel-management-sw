package de.oth.muskelmanagement.controller.api;

import de.oth.muskelmanagement.dto.PricingDto;
import de.oth.muskelmanagement.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pricing")
@PreAuthorize("hasRole('ADMIN')")
public class PricingRestController {

    private final PricingService pricingService;

    public PricingRestController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public ResponseEntity<Page<PricingDto>> findPricings(@RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active, @PageableDefault(size = 10) Pageable pageable) {
        Page<PricingDto> pricings = pricingService.findPricings(name, active, pageable);
        return ResponseEntity.ok(pricings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricingDto> getPricingById(@PathVariable Long id) {
        PricingDto pricing = pricingService.findById(id);
        return ResponseEntity.ok(pricing);
    }

    @PostMapping
    public ResponseEntity<PricingDto> createPricing(@Valid @RequestBody PricingDto pricingDto) {
        pricingService.save(pricingDto);
        return new ResponseEntity<>(pricingDto, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricingDto> updatePricing(@PathVariable Long id, @Valid @RequestBody PricingDto pricingDto) {
        pricingDto.setId(id);
        pricingService.updatePricing(pricingDto);
        return ResponseEntity.ok(pricingDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricing(@PathVariable Long id) {
        pricingService.deletePricing(id);
        return ResponseEntity.noContent().build();
    }
}

