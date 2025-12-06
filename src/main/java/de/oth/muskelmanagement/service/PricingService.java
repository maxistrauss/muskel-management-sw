package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.PricingDto;
import de.oth.muskelmanagement.model.entity.Pricing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PricingService {
    Pricing save(PricingDto pricingDto);

    Pricing findByName(String name);

    Page<PricingDto> findAll(Pageable pageable);

    PricingDto findById(Long id);

    void updatePricing(PricingDto pricingDto);

    void deletePricing(Long id);

    Page<PricingDto> findPricings(String name, Boolean active, Pageable pageable);
}

