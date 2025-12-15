package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
