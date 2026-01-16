package de.oth.muskelmanagement.controller.web.general;

import de.oth.muskelmanagement.dto.TrainingPlanDto;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.model.entity.TrainingPlan;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.repository.ExerciseRepository;
import de.oth.muskelmanagement.service.TrainingPlanService;
import de.oth.muskelmanagement.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class TrainingPlanController {

    private final TrainingPlanService trainingPlanService;
    private final UserService userService;
    private final ExerciseRepository exerciseRepository;

    public TrainingPlanController(TrainingPlanService trainingPlanService, UserService userService,
            ExerciseRepository exerciseRepository) {
        this.trainingPlanService = trainingPlanService;
        this.userService = userService;
        this.exerciseRepository = exerciseRepository;
    }

    // --- TRAINER ROUTES ---

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @GetMapping("/trainer/members/{memberId}/plans")
    public String getPlansForMember(@PathVariable Long memberId, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        User member = userService.findEntityById(memberId);

        // If logged in user is only trainer (not admin), they cannot see admin's plans
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean targetIsAdmin = member.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && targetIsAdmin) {
            throw new AccessDeniedException("Trainers cannot view admin plans");
        }
        
        List<TrainingPlan> plans = trainingPlanService.getPlansByMember(memberId);
        model.addAttribute("member", member);
        model.addAttribute("plans", plans);
        return "trainer/training-plans";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @GetMapping("/trainer/members/{memberId}/plans/create")
    public String createPlanForm(@PathVariable Long memberId, Model model) {
        User member = userService.findEntityById(memberId);
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setMemberId(memberId);

        List<Exercise> allExercises = exerciseRepository.findAll();

        model.addAttribute("member", member);
        model.addAttribute("trainingPlan", dto);
        model.addAttribute("exercises", allExercises);
        return "trainer/training-plan-form";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/members/{memberId}/plans/create")
    public String createPlan(@PathVariable Long memberId, @ModelAttribute TrainingPlanDto trainingPlanDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User trainer = userService.findByEmail(userDetails.getUsername());
        if (trainer == null)
            throw new IllegalArgumentException("Trainer not found");

        trainingPlanDto.setMemberId(memberId);
        trainingPlanService.createPlan(trainingPlanDto, trainer);
        return "redirect:/trainer/members/" + memberId + "/plans";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @GetMapping("/trainer/plans/{planId}/edit")
    public String editPlanForm(@PathVariable Long planId, Model model) {
        TrainingPlan plan = trainingPlanService.getPlanById(planId);
        TrainingPlanDto dto = trainingPlanService.toDto(plan);

        List<Exercise> allExercises = exerciseRepository.findAll();

        model.addAttribute("member", plan.getMember());
        model.addAttribute("trainingPlan", dto);
        model.addAttribute("exercises", allExercises);
        model.addAttribute("isEdit", true);
        return "trainer/training-plan-form";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/plans/{planId}/edit")
    public String updatePlan(@PathVariable Long planId, @ModelAttribute TrainingPlanDto trainingPlanDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User trainer = userService.findByEmail(userDetails.getUsername());
        if (trainer == null)
            throw new IllegalArgumentException("Trainer not found");

        TrainingPlan updatedPlan = trainingPlanService.updatePlan(planId, trainingPlanDto, trainer);
        return "redirect:/trainer/members/" + updatedPlan.getMember().getId() + "/plans";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/plans/{planId}/archive")
    public String archivePlan(@PathVariable Long planId) {
        TrainingPlan plan = trainingPlanService.getPlanById(planId);
        Long memberId = plan.getMember().getId();
        trainingPlanService.archivePlan(planId);
        return "redirect:/trainer/members/" + memberId + "/plans";
    }

    @PreAuthorize("hasRole('TRAINER') or hasRole('ADMIN')")
    @PostMapping("/trainer/plans/{planId}/unarchive")
    public String unarchivePlan(@PathVariable Long planId) {
        TrainingPlan plan = trainingPlanService.getPlanById(planId);
        Long memberId = plan.getMember().getId();
        trainingPlanService.unarchivePlan(planId);
        return "redirect:/trainer/members/" + memberId + "/plans";
    }

    // --- MEMBER ROUTES ---

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member/plans")
    public String getMyPlans(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User member = userService.findByEmail(userDetails.getUsername());
        if (member == null)
            throw new IllegalArgumentException("Member not found");

        List<TrainingPlan> plans = trainingPlanService.getActivePlansByMember(member.getId());
        model.addAttribute("plans", plans);
        return "member/training-plans";
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member/plans/{planId}")
    public String getPlanDetails(@PathVariable Long planId, @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        User member = userService.findByEmail(userDetails.getUsername());
        if (member == null)
            throw new IllegalArgumentException("Member not found");

        TrainingPlan plan = trainingPlanService.getPlanById(planId);

        // Security check: Ensure plan belongs to member
        if (!plan.getMember().getId().equals(member.getId())) {
            return "redirect:/member/plans?error=access_denied";
        }

        model.addAttribute("plan", plan);
        return "member/training-plan-details";
    }
}
