package de.oth.muskelmanagement.controller.web.general;

import de.oth.muskelmanagement.dto.FitnessMeasurementDto;
import de.oth.muskelmanagement.model.entity.FitnessMeasurement;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.service.FitnessMeasurementService;
import de.oth.muskelmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class FitnessMeasurementController {

    private final FitnessMeasurementService measurementService;
    private final UserService userService;

    public FitnessMeasurementController(FitnessMeasurementService measurementService, UserService userService) {
        this.measurementService = measurementService;
        this.userService = userService;
    }

    // --- TRAINER ROUTES ---

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @GetMapping("/trainer/members/{memberId}/measurements")
    public String getMeasurementsForMember(@PathVariable Long memberId, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        User member = userService.findEntityById(memberId);

        // If logged in user is only trainer (not admin), they cannot see admin's measurements
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean targetIsAdmin = member.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && targetIsAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Trainers cannot view admin measurements");
        }

        List<FitnessMeasurement> measurements = measurementService.getMeasurementsByUser(memberId);
        List<FitnessMeasurementDto> measurementDtos = measurements.stream().map(measurementService::toDto).toList();
        model.addAttribute("member", member);
        model.addAttribute("measurements", measurementDtos);
        return "trainer/measurements";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @GetMapping("/trainer/members/{memberId}/measurements/create")
    public String createMeasurementForm(@PathVariable Long memberId, Model model) {
        User member = userService.findEntityById(memberId);
        FitnessMeasurementDto dto = new FitnessMeasurementDto();
        dto.setUserId(memberId);
        dto.setDate(LocalDate.now());

        model.addAttribute("member", member);
        model.addAttribute("measurement", dto);
        return "trainer/measurement-form";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/members/{memberId}/measurements/create")
    public String createMeasurement(@PathVariable Long memberId,
            @Valid @ModelAttribute("measurement") FitnessMeasurementDto dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            User member = userService.findEntityById(memberId);
            model.addAttribute("member", member);
            return "trainer/measurement-form";
        }
        dto.setUserId(memberId);
        measurementService.createMeasurement(dto);
        return "redirect:/trainer/members/" + memberId + "/measurements";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @GetMapping("/trainer/measurements/{id}/edit")
    public String editMeasurementForm(@PathVariable Long id, Model model) {
        FitnessMeasurement measurement = measurementService.getMeasurementById(id);
        FitnessMeasurementDto dto = measurementService.toDto(measurement);

        model.addAttribute("measurement", dto);
        model.addAttribute("member", measurement.getUser());
        model.addAttribute("isEdit", true);
        return "trainer/measurement-form";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/measurements/{id}/edit")
    public String updateMeasurement(@PathVariable Long id,
            @Valid @ModelAttribute("measurement") FitnessMeasurementDto dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // Need to reload member for the template
            FitnessMeasurement existing = measurementService.getMeasurementById(id);
            model.addAttribute("member", existing.getUser());
            model.addAttribute("isEdit", true);
            return "trainer/measurement-form";
        }
        FitnessMeasurement updated = measurementService.updateMeasurement(id, dto);
        return "redirect:/trainer/members/" + updated.getUser().getId() + "/measurements";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/measurements/{id}/delete")
    public String deleteMeasurement(@PathVariable Long id) {
        FitnessMeasurement measurement = measurementService.getMeasurementById(id);
        Long userId = measurement.getUser().getId();
        measurementService.deleteMeasurement(id);
        return "redirect:/trainer/members/" + userId + "/measurements";
    }

    // --- MEMBER ROUTES ---
    // (Optional: Members can view their own progress)
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member/measurements")
    public String getMyMeasurements(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User member = userService.findByEmail(userDetails.getUsername());
        if (member == null)
            throw new IllegalArgumentException("Member not found");

        List<FitnessMeasurement> measurements = measurementService.getMeasurementsByUser(member.getId());
        List<FitnessMeasurementDto> measurementDtos = measurements.stream().map(measurementService::toDto).toList();
        model.addAttribute("measurements", measurementDtos);
        return "member/measurements";
    }
}
