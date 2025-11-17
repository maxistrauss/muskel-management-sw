package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.Tarif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TarifRepository extends JpaRepository<Tarif, Long>, JpaSpecificationExecutor<Tarif> {
    Tarif findByName(String name);
}

