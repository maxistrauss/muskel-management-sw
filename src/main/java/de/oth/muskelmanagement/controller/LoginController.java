package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.service.UserService;
import de.oth.muskelmanagement.service.dto.RegistrationDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        RegistrationDto registrationDto = new RegistrationDto();
        model.addAttribute("registrationDto", registrationDto);
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationDto") RegistrationDto registrationDto,
                               BindingResult result,
                               @RequestParam("confirmPassword") String confirmPassword,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        // Check if user already exists
        if (userService.findByEmail(registrationDto.getEmail()) != null) {
            model.addAttribute("error", "A user with this email already exists!");
            return "register";
        }

        // Validate that passwords match
        if (!registrationDto.getPassword().equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "register";
        }
        
        if (result.hasErrors()) {
            return "register";
        }

        try {
            // Register the user with ROLE_MEMBER (set automatically in service)
            userService.registerUser(registrationDto);
            
            redirectAttributes.addFlashAttribute("success", "Registration successful! You can now log in.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "register";
        }
    }
}
