package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.PricingDto;
import de.oth.muskelmanagement.model.entity.Pricing;
import de.oth.muskelmanagement.repository.PricingRepository;
import de.oth.muskelmanagement.service.PricingService;
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
public class PricingServiceImpl implements PricingService {

    private final PricingRepository pricingRepository;

    public PricingServiceImpl(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    @Override
    public Pricing save(PricingDto pricingDto) {
        Pricing pricing = new Pricing();
        pricing.setName(pricingDto.getName());
        pricing.setPrice(pricingDto.getPrice());
        pricing.setDurationMonths(pricingDto.getDurationMonths());
        pricing.setDescription(pricingDto.getDescription());
        pricing.setActive(pricingDto.isActive());
        return pricingRepository.save(pricing);
    }

    @Override
    public Pricing findByName(String name) {
        return pricingRepository.findByName(name);
    }

    @Override
    public Page<PricingDto> findAll(Pageable pageable) {
        return pricingRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public PricingDto findById(Long id) {
        Pricing pricing = pricingRepository.findById(id).orElseThrow(() -> new RuntimeException("Pricing not found"));
        return convertToDto(pricing);
    }

    @Override
    public void updatePricing(PricingDto pricingDto) {
        Pricing pricing = pricingRepository.findById(pricingDto.getId())
                .orElseThrow(() -> new RuntimeException("Pricing not found"));
        pricing.setName(pricingDto.getName());
        pricing.setPrice(pricingDto.getPrice());
        pricing.setDurationMonths(pricingDto.getDurationMonths());
        pricing.setDescription(pricingDto.getDescription());
        pricing.setActive(pricingDto.isActive());
        pricingRepository.save(pricing);
    }

    @Override
    public void deletePricing(Long id) {
        pricingRepository.deleteById(id);
    }

    @Override
    public Page<PricingDto> findPricings(String name, Boolean active, Pageable pageable) {
        Specification<Pricing> spec = (Root<Pricing> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return pricingRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    private PricingDto convertToDto(Pricing pricing) {
        PricingDto pricingDto = new PricingDto();
        pricingDto.setId(pricing.getId());
        pricingDto.setName(pricing.getName());
        pricingDto.setPrice(pricing.getPrice());
        pricingDto.setDurationMonths(pricing.getDurationMonths());
        pricingDto.setDescription(pricing.getDescription());
        pricingDto.setActive(pricing.isActive());
        return pricingDto;
    }
}

