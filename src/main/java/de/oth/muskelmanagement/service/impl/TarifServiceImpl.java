package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.TarifDto;
import de.oth.muskelmanagement.model.entity.Tarif;
import de.oth.muskelmanagement.repository.TarifRepository;
import de.oth.muskelmanagement.service.TarifService;
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
public class TarifServiceImpl implements TarifService {

    private final TarifRepository tarifRepository;

    public TarifServiceImpl(TarifRepository tarifRepository) {
        this.tarifRepository = tarifRepository;
    }

    @Override
    public Tarif save(TarifDto tarifDto) {
        Tarif tarif = new Tarif();
        tarif.setName(tarifDto.getName());
        tarif.setPrice(tarifDto.getPrice());
        tarif.setDurationMonths(tarifDto.getDurationMonths());
        tarif.setDescription(tarifDto.getDescription());
        tarif.setActive(tarifDto.isActive());
        return tarifRepository.save(tarif);
    }

    @Override
    public Tarif findByName(String name) {
        return tarifRepository.findByName(name);
    }

    @Override
    public Page<TarifDto> findAll(Pageable pageable) {
        return tarifRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public TarifDto findById(Long id) {
        Tarif tarif = tarifRepository.findById(id).orElseThrow(() -> new RuntimeException("Tarif not found"));
        return convertToDto(tarif);
    }

    @Override
    public void updateTarif(TarifDto tarifDto) {
        Tarif tarif = tarifRepository.findById(tarifDto.getId())
                .orElseThrow(() -> new RuntimeException("Tarif not found"));
        tarif.setName(tarifDto.getName());
        tarif.setPrice(tarifDto.getPrice());
        tarif.setDurationMonths(tarifDto.getDurationMonths());
        tarif.setDescription(tarifDto.getDescription());
        tarif.setActive(tarifDto.isActive());
        tarifRepository.save(tarif);
    }

    @Override
    public void deleteTarif(Long id) {
        tarifRepository.deleteById(id);
    }

    @Override
    public Page<TarifDto> findTarifs(String name, Boolean active, Pageable pageable) {
        Specification<Tarif> spec = (Root<Tarif> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return tarifRepository.findAll(spec, pageable).map(this::convertToDto);
    }

    private TarifDto convertToDto(Tarif tarif) {
        TarifDto tarifDto = new TarifDto();
        tarifDto.setId(tarif.getId());
        tarifDto.setName(tarif.getName());
        tarifDto.setPrice(tarif.getPrice());
        tarifDto.setDurationMonths(tarif.getDurationMonths());
        tarifDto.setDescription(tarif.getDescription());
        tarifDto.setActive(tarif.isActive());
        return tarifDto;
    }
}

