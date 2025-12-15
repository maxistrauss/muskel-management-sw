package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.ReportDataDto;
import de.oth.muskelmanagement.model.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {
    Page<Report> findAll(Pageable pageable);

    Report findById(Long id);

    Report save(Report report);

    void delete(Long id);

    ReportDataDto generateReportData(Report report);
}
