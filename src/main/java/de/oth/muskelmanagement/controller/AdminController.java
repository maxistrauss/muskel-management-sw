package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.service.UserService;
import de.oth.muskelmanagement.service.dto.UserDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public String listUsers(Model model, Principal principal, @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String email, @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName, @RequestParam(required = false) String membershipType) {

        Page<UserDto> userPage = userService.findUsers(email, firstName, lastName, membershipType, pageable);

        model.addAttribute("userPage", userPage);
        model.addAttribute("users", userPage.getContent()); // For compatibility with existing table iteration
        model.addAttribute("currentPage", userPage.getNumber() + 1);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("currentUsername", principal.getName());

        // Add search parameters back to model for form persistence
        model.addAttribute("email", email);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("membershipType", membershipType);

        return "admin/users";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new UserDto());
        model.addAttribute("allRoles", Arrays.asList("ROLE_ADMIN", "ROLE_TRAINER", "ROLE_MEMBER"));
        return "admin/user-form";
    }

    @PostMapping("/users/new")
    public String createUser(@Valid @ModelAttribute("user") UserDto userDto, BindingResult bindingResult, Model model) {
        if (userDto.getPassword() == null || userDto.getPassword().isBlank()) {
            bindingResult.rejectValue("password", "password.notblank", "Password cannot be blank");
        } else if (userDto.getPassword().length() < 8) {
            bindingResult.rejectValue("password", "password.size", "Password must be at least 8 characters long");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", Arrays.asList("ROLE_ADMIN", "ROLE_TRAINER", "ROLE_MEMBER"));
            return "admin/user-form";
        }
        userService.save(userDto);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, Principal principal) {
        UserDto user = userService.findById(id);
        if (user.getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("Admins are not allowed to edit their own account.");
        }
        model.addAttribute("user", user);
        model.addAttribute("allRoles", Arrays.asList("ROLE_ADMIN", "ROLE_TRAINER", "ROLE_MEMBER"));
        return "admin/user-form";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable("id") Long id, @Valid @ModelAttribute("user") UserDto userDto,
            BindingResult bindingResult, Model model, Principal principal) {
        UserDto existingUser = userService.findById(id); // Get existing user to compare email
        if (existingUser.getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("Admins are not allowed to update their own account.");
        }
        
        // Validate deactivation reason when disabling an account
        if (!userDto.isEnabled() && (userDto.getDeactivationReason() == null || userDto.getDeactivationReason().isBlank())) {
            bindingResult.rejectValue("deactivationReason", "deactivationReason.required",
                "Deactivation reason is required when disabling a user account.");
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("allRoles", Arrays.asList("ROLE_ADMIN", "ROLE_TRAINER", "ROLE_MEMBER"));
            return "admin/user-form";
        }
        userDto.setId(id);
        userService.updateUser(userDto);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, Principal principal) {
        UserDto userToDelete = userService.findById(id);
        if (userToDelete.getEmail().equals(principal.getName())) {
            throw new AccessDeniedException("Admins are not allowed to delete their own account.");
        }
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
