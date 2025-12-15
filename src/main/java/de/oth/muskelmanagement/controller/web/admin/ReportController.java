package de.oth.muskelmanagement.controller.web.admin;

import de.oth.muskelmanagement.dto.ReportDataDto;
import de.oth.muskelmanagement.model.entity.Report;
import de.oth.muskelmanagement.model.enums.ReportType;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.service.ReportService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    private final ReportService reportService;
    private final CourseService courseService;

    public ReportController(ReportService reportService, CourseService courseService) {
        this.reportService = reportService;
        this.courseService = courseService;
    }

    @GetMapping
    public String listReports(Model model, @PageableDefault(size = 10) Pageable pageable) {
        model.addAttribute("reportPage", reportService.findAll(pageable));
        return "admin/reports";
    }

    @GetMapping("/new")
    public String createReportForm(Model model) {
        model.addAttribute("report", new Report());
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("courses", courseService.findAll(Pageable.unpaged()).getContent());
        return "admin/report-form";
    }

    @PostMapping("/save")
    public String saveReport(@ModelAttribute Report report) {
        reportService.save(report);
        return "redirect:/admin/reports";
    }

    @GetMapping("/edit/{id}")
    public String editReportForm(@PathVariable Long id, Model model) {
        model.addAttribute("report", reportService.findById(id));
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("courses", courseService.findAll(Pageable.unpaged()).getContent());
        return "admin/report-form";
    }

    @GetMapping("/delete/{id}")
    public String deleteReport(@PathVariable Long id) {
        reportService.delete(id);
        return "redirect:/admin/reports";
    }

    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        Report report = reportService.findById(id);
        ReportDataDto data = reportService.generateReportData(report);

        model.addAttribute("report", report);
        model.addAttribute("chartData", data);
        return "admin/report-view";
    }
}
