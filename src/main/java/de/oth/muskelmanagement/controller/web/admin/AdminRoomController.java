package de.oth.muskelmanagement.controller.web.admin;

import de.oth.muskelmanagement.dto.RoomDto;
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
@RequestMapping("/admin/rooms")
public class AdminRoomController {

    private final RoomService roomService;

    public AdminRoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(Model model, @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String name, @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity, @RequestParam(required = false) Boolean active) {

        Page<RoomDto> roomPage = roomService.searchRooms(name, minCapacity, maxCapacity, active, pageable);

        model.addAttribute("roomPage", roomPage);
        model.addAttribute("rooms", roomPage.getContent());
        model.addAttribute("currentPage", roomPage.getNumber() + 1);
        model.addAttribute("totalPages", roomPage.getTotalPages());
        model.addAttribute("totalItems", roomPage.getTotalElements());

        // Add search parameters back to model for form persistence
        model.addAttribute("name", name);
        model.addAttribute("minCapacity", minCapacity);
        model.addAttribute("maxCapacity", maxCapacity);
        model.addAttribute("active", active);

        return "admin/rooms";
    }

    @GetMapping("/new")
    public String showCreateRoomForm(Model model) {
        model.addAttribute("room", new RoomDto());
        return "admin/room-form";
    }

    @PostMapping("/new")
    public String createRoom(@Valid @ModelAttribute("room") RoomDto roomDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/room-form";
        }
        roomService.save(roomDto);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/edit/{id}")
    public String showEditRoomForm(@PathVariable("id") Long id, Model model) {
        RoomDto room = roomService.findById(id);
        model.addAttribute("room", room);
        return "admin/room-form";
    }

    @PostMapping("/edit/{id}")
    public String updateRoom(@PathVariable("id") Long id, @Valid @ModelAttribute("room") RoomDto roomDto,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "admin/room-form";
        }
        roomDto.setId(id);
        roomService.update(roomDto);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/deactivate/{id}")
    public String deactivateRoom(@PathVariable("id") Long id) {
        roomService.deactivate(id);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable("id") Long id) {
        roomService.delete(id);
        return "redirect:/admin/rooms";
    }

    @GetMapping("/view/{id}")
    public String viewRoom(@PathVariable("id") Long id, Model model) {
        RoomDto room = roomService.findById(id);
        model.addAttribute("room", room);
        return "admin/room-details";
    }
}
