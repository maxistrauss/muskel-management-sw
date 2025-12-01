package de.oth.muskelmanagement.controller;

import de.oth.muskelmanagement.service.UserService;
import de.oth.muskelmanagement.service.CourseService;
import de.oth.muskelmanagement.model.Course;
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
    private final CourseService courseService;
    private final de.oth.muskelmanagement.service.RoomService roomService;
    private final de.oth.muskelmanagement.repository.UserRepository userRepository;
    private final de.oth.muskelmanagement.repository.RoomRepository roomRepository;

    public AdminController(UserService userService, CourseService courseService,
                          de.oth.muskelmanagement.service.RoomService roomService,
                          de.oth.muskelmanagement.repository.UserRepository userRepository,
                          de.oth.muskelmanagement.repository.RoomRepository roomRepository) {
        this.userService = userService;
        this.courseService = courseService;
        this.roomService = roomService;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
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

    // --- Courses management ---
    @GetMapping("/courses")
    public String listCourses(Model model, @PageableDefault(size = 10) Pageable pageable, 
                             @RequestParam(required = false) Long trainerId) {
        var page = courseService.findAll(pageable);
        var courses = page.getContent();
        
        // Filter by trainer if trainerId is provided
        if (trainerId != null) {
            courses = courses.stream()
                    .filter(course -> course.getTrainer() != null && course.getTrainer().getId().equals(trainerId))
                    .toList();
        }
        
        model.addAttribute("coursePage", page);
        model.addAttribute("courses", courses);
        model.addAttribute("currentPage", page.getNumber() + 1);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        
        // Add trainers to model for dropdown filter
        var allUsers = userService.findAll(Pageable.unpaged());
        var trainers = allUsers.getContent().stream()
                .filter(userDto -> userDto.getRoles() != null && userDto.getRoles().contains("ROLE_TRAINER"))
                .toList();
        model.addAttribute("trainers", trainers);
        model.addAttribute("selectedTrainerId", trainerId);
        
        return "admin/courses";
    }

    @GetMapping("/courses/new")
    public String showCreateCourseForm(Model model) {
        model.addAttribute("course", new Course());
        
        // Add trainers to model for dropdown
        var allUsers = userService.findAll(Pageable.unpaged());
        var trainers = allUsers.getContent().stream()
                .filter(userDto -> userDto.getRoles() != null && userDto.getRoles().contains("ROLE_TRAINER"))
                .toList();
        model.addAttribute("trainers", trainers);
        
        // Add rooms for selection
        var rooms = roomService.findActiveRooms();
        model.addAttribute("rooms", rooms);
        
        return "admin/course-form";
    }

    @PostMapping("/courses/new")
    public String createCourse(@ModelAttribute("course") Course course) {
        courseService.save(course);
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String showEditCourseForm(@PathVariable Long id, Model model) {
        Course course = courseService.findById(id);
        model.addAttribute("course", course);
        
        // Add trainers to model for dropdown
        var allUsers = userService.findAll(Pageable.unpaged());
        var trainers = allUsers.getContent().stream()
                .filter(userDto -> userDto.getRoles() != null && userDto.getRoles().contains("ROLE_TRAINER"))
                .toList();
        model.addAttribute("trainers", trainers);
        
        // Add rooms for selection
        var rooms = roomService.findActiveRooms();
        model.addAttribute("rooms", rooms);
        
        return "admin/course-form";
    }

    @PostMapping("/courses/edit/{id}")
    public String updateCourse(@PathVariable Long id, @ModelAttribute("course") Course course) {
        Course existing = courseService.findById(id);
        existing.setName(course.getName());
        existing.setDescription(course.getDescription());
        existing.setCapacity(course.getCapacity());
        existing.setActive(course.isActive());
        
        // Load trainer from repository if ID is provided
        if (course.getTrainer() != null && course.getTrainer().getId() != null) {
            existing.setTrainer(userRepository.findById(course.getTrainer().getId()).orElse(null));
        } else {
            existing.setTrainer(null);
        }
        
        // Load room from repository if ID is provided
        if (course.getRoom() != null && course.getRoom().getId() != null) {
            existing.setRoom(roomRepository.findById(course.getRoom().getId()).orElse(null));
        } else {
            existing.setRoom(null);
        }
        
        courseService.save(existing);
        return "redirect:/admin/courses";
    }

    @GetMapping("/courses/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteById(id);
        return "redirect:/admin/courses";
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
