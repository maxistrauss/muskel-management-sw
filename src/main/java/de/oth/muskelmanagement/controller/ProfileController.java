package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.model.Role;
import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.service.UserService;
import de.oth.muskelmanagement.service.dto.UserDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        UserDto userDto = convertToDto(user);
        model.addAttribute("user", userDto);
        model.addAttribute("isViewMode", true);
        return "profile";
    }

    @GetMapping("/edit")
    public String showEditProfile(Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        UserDto userDto = convertToDto(user);
        model.addAttribute("user", userDto);
        model.addAttribute("isViewMode", false);
        return "profile";
    }

    @PostMapping("/edit")
    public String updateProfile(@ModelAttribute("user") UserDto userDto,
                                BindingResult bindingResult, Model model, Principal principal) {
        User user = userService.findByEmail(principal.getName());
        UserDto currentUser = convertToDto(user);
        
        // Ensure user can only update their own profile
        if (!currentUser.getId().equals(userDto.getId())) {
            model.addAttribute("error", "You can only edit your own profile");
            model.addAttribute("isViewMode", false);
            return "profile";
        }
        
        // Preserve fields that shouldn't be changed via profile edit
        userDto.setId(currentUser.getId());
        userDto.setEmail(currentUser.getEmail());
        userDto.setMembershipType(currentUser.getMembershipType());
        userDto.setRoles(currentUser.getRoles());
        userDto.setEnabled(currentUser.isEnabled());
        
        // Manual validation for firstName and lastName only
        if (userDto.getFirstName() == null || userDto.getFirstName().trim().isEmpty()) {
            model.addAttribute("error", "First name cannot be blank");
            model.addAttribute("isViewMode", false);
            return "profile";
        }
        
        if (userDto.getLastName() == null || userDto.getLastName().trim().isEmpty()) {
            model.addAttribute("error", "Last name cannot be blank");
            model.addAttribute("isViewMode", false);
            return "profile";
        }
        
        userService.updateUser(userDto);
        return "redirect:/profile";
    }

    @PostMapping("/deactivate")
    public String deactivateAccount(Principal principal, HttpServletRequest request,
                                   HttpServletResponse response, RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(principal.getName());
        
        if (user != null) {
            // Deactivate the user account
            UserDto userDto = convertToDto(user);
            userDto.setEnabled(false);
            userService.updateUser(userDto);
            
            // Log out the user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            
            redirectAttributes.addFlashAttribute("success", "Your account has been deactivated. Please contact an administrator to reactivate it.");
        }
        
        return "redirect:/login";
    }

    private UserDto convertToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setMembershipType(user.getMembershipType());
        userDto.setEnabled(user.isEnabled());
        userDto.setRoles(user.getRoles().stream().map(role -> role.getName()).collect(java.util.stream.Collectors.toSet()));
        return userDto;
    }
}