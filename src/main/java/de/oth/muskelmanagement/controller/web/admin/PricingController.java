package de.oth.muskelmanagement.controller.web.admin;

import de.oth.muskelmanagement.dto.PricingDto;
import de.oth.muskelmanagement.service.PricingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @GetMapping
    public String listPricings(Model model, @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String name, @RequestParam(required = false) Boolean active) {

        Page<PricingDto> pricingPage = pricingService.findPricings(name, active, pageable);

        model.addAttribute("pricingPage", pricingPage);
        model.addAttribute("pricings", pricingPage.getContent());
        model.addAttribute("currentPage", pricingPage.getNumber() + 1);
        model.addAttribute("totalPages", pricingPage.getTotalPages());
        model.addAttribute("totalItems", pricingPage.getTotalElements());

        // Add search parameters back to model for form persistence
        model.addAttribute("name", name);
        model.addAttribute("active", active);

        return "admin/pricings";
    }

    @GetMapping("/new")
    public String showCreatePricingForm(Model model) {
        model.addAttribute("pricing", new PricingDto());
        return "admin/pricing-form";
    }

    @PostMapping("/new")
    public String createPricing(@Valid @ModelAttribute("pricing") PricingDto pricingDto, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/pricing-form";
        }
        pricingService.save(pricingDto);
        return "redirect:/admin/pricing";
    }

    @GetMapping("/edit/{id}")
    public String showEditPricingForm(@PathVariable("id") Long id, Model model) {
        PricingDto pricing = pricingService.findById(id);
        model.addAttribute("pricing", pricing);
        return "admin/pricing-form";
    }

    @PostMapping("/edit/{id}")
    public String updatePricing(@PathVariable("id") Long id, @Valid @ModelAttribute("pricing") PricingDto pricingDto,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/pricing-form";
        }
        pricingDto.setId(id);
        pricingService.updatePricing(pricingDto);
        return "redirect:/admin/pricing";
    }

    @GetMapping("/delete/{id}")
    public String deletePricing(@PathVariable("id") Long id) {
        pricingService.deletePricing(id);
        return "redirect:/admin/pricing";
    }
}

