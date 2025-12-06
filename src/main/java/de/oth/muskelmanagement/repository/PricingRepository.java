package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PricingRepository extends JpaRepository<Pricing, Long>, JpaSpecificationExecutor<Pricing> {
    Pricing findByName(String name);
}

