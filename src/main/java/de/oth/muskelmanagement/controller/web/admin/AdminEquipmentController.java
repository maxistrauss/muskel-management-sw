package de.oth.muskelmanagement.controller.web.admin;

import de.oth.muskelmanagement.dto.EquipmentDto;
import de.oth.muskelmanagement.model.enums.EquipmentCategory;
import de.oth.muskelmanagement.model.enums.EquipmentStatus;
import de.oth.muskelmanagement.service.EquipmentService;
import de.oth.muskelmanagement.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/equipment")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;
    private final RoomService roomService;

    public AdminEquipmentController(EquipmentService equipmentService, RoomService roomService) {
        this.equipmentService = equipmentService;
        this.roomService = roomService;
    }

    @GetMapping
    public String listEquipment(Model model, @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String name, @RequestParam(required = false) String serialNumber,
            @RequestParam(required = false) String manufacturer, @RequestParam(required = false) EquipmentStatus status,
            @RequestParam(required = false) String location) {

        Page<EquipmentDto> equipmentPage = equipmentService.findEquipment(name, serialNumber, manufacturer, status,
                location, pageable);

        model.addAttribute("equipmentPage", equipmentPage);
        model.addAttribute("equipment", equipmentPage.getContent());
        model.addAttribute("currentPage", equipmentPage.getNumber() + 1);
        model.addAttribute("totalPages", equipmentPage.getTotalPages());
        model.addAttribute("totalItems", equipmentPage.getTotalElements());

        // Add search parameters back to model for form persistence
        model.addAttribute("name", name);
        model.addAttribute("serialNumber", serialNumber);
        model.addAttribute("manufacturer", manufacturer);
        model.addAttribute("status", status);
        model.addAttribute("location", location);

        // Add all statuses for filter dropdown
        model.addAttribute("allStatuses", EquipmentStatus.values());

        return "admin/equipment";
    }

    @GetMapping("/new")
    public String showCreateEquipmentForm(Model model) {
        model.addAttribute("equipment", new EquipmentDto());
        model.addAttribute("allStatuses", EquipmentStatus.values());
        model.addAttribute("allCategories", EquipmentCategory.values());
        model.addAttribute("allRooms", roomService.findActiveRooms());
        return "admin/equipment-form";
    }

    @PostMapping("/new")
    public String createEquipment(@Valid @ModelAttribute("equipment") EquipmentDto equipmentDto,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allStatuses", EquipmentStatus.values());
            model.addAttribute("allCategories", EquipmentCategory.values());
            model.addAttribute("allRooms", roomService.findActiveRooms());
            return "admin/equipment-form";
        }
        equipmentService.save(equipmentDto);
        return "redirect:/admin/equipment";
    }

    @GetMapping("/edit/{id}")
    public String showEditEquipmentForm(@PathVariable("id") Long id, Model model) {
        EquipmentDto equipment = equipmentService.findById(id);
        model.addAttribute("equipment", equipment);
        model.addAttribute("allStatuses", EquipmentStatus.values());
        model.addAttribute("allCategories", EquipmentCategory.values());
        model.addAttribute("allRooms", roomService.findActiveRooms());
        return "admin/equipment-form";
    }

    @PostMapping("/edit/{id}")
    public String updateEquipment(@PathVariable("id") Long id,
            @Valid @ModelAttribute("equipment") EquipmentDto equipmentDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("allStatuses", EquipmentStatus.values());
            model.addAttribute("allCategories", EquipmentCategory.values());
            model.addAttribute("allRooms", roomService.findActiveRooms());
            return "admin/equipment-form";
        }
        equipmentDto.setId(id);
        equipmentService.updateEquipment(equipmentDto);
        return "redirect:/admin/equipment";
    }

    @GetMapping("/archive/{id}")
    public String archiveEquipment(@PathVariable("id") Long id) {
        equipmentService.archiveEquipment(id);
        return "redirect:/admin/equipment";
    }

    @GetMapping("/delete/{id}")
    public String deleteEquipment(@PathVariable("id") Long id) {
        equipmentService.deleteEquipment(id);
        return "redirect:/admin/equipment";
    }
}
