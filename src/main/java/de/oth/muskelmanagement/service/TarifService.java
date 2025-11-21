package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.Tarif;
import de.oth.muskelmanagement.service.dto.TarifDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TarifService {
    Tarif save(TarifDto tarifDto);

    Tarif findByName(String name);

    Page<TarifDto> findAll(Pageable pageable);

    TarifDto findById(Long id);

    void updateTarif(TarifDto tarifDto);

    void deleteTarif(Long id);

    Page<TarifDto> findTarifs(String name, Boolean active, Pageable pageable);
}

