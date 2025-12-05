package de.oth.muskelmanagement.controller.web.admin;

import de.oth.muskelmanagement.dto.TarifDto;
import de.oth.muskelmanagement.service.TarifService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/tarifs")
public class TarifController {

    private final TarifService tarifService;

    public TarifController(TarifService tarifService) {
        this.tarifService = tarifService;
    }

    @GetMapping
    public String listTarifs(Model model, @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String name, @RequestParam(required = false) Boolean active) {

        Page<TarifDto> tarifPage = tarifService.findTarifs(name, active, pageable);

        model.addAttribute("tarifPage", tarifPage);
        model.addAttribute("tarifs", tarifPage.getContent());
        model.addAttribute("currentPage", tarifPage.getNumber() + 1);
        model.addAttribute("totalPages", tarifPage.getTotalPages());
        model.addAttribute("totalItems", tarifPage.getTotalElements());

        // Add search parameters back to model for form persistence
        model.addAttribute("name", name);
        model.addAttribute("active", active);

        return "admin/tarifs";
    }

    @GetMapping("/new")
    public String showCreateTarifForm(Model model) {
        model.addAttribute("tarif", new TarifDto());
        return "admin/tariff-form";
    }

    @PostMapping("/new")
    public String createTarif(@Valid @ModelAttribute("tarif") TarifDto tarifDto, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/tariff-form";
        }
        tarifService.save(tarifDto);
        return "redirect:/admin/tarifs";
    }

    @GetMapping("/edit/{id}")
    public String showEditTarifForm(@PathVariable("id") Long id, Model model) {
        TarifDto tarif = tarifService.findById(id);
        model.addAttribute("tarif", tarif);
        return "admin/tariff-form";
    }

    @PostMapping("/edit/{id}")
    public String updateTarif(@PathVariable("id") Long id, @Valid @ModelAttribute("tarif") TarifDto tarifDto,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/tariff-form";
        }
        tarifDto.setId(id);
        tarifService.updateTarif(tarifDto);
        return "redirect:/admin/tarifs";
    }

    @GetMapping("/delete/{id}")
    public String deleteTarif(@PathVariable("id") Long id) {
        tarifService.deleteTarif(id);
        return "redirect:/admin/tarifs";
    }
}

