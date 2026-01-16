package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.dto.TrainingPlanDto;
import de.oth.muskelmanagement.dto.TrainingPlanExerciseDto;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.model.entity.TrainingPlan;
import de.oth.muskelmanagement.model.entity.TrainingPlanExercise;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.TrainingPlanStatus;
import de.oth.muskelmanagement.repository.ExerciseRepository;
import de.oth.muskelmanagement.repository.TrainingPlanRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.TrainingPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainingPlanServiceImpl implements TrainingPlanService {

    private final TrainingPlanRepository trainingPlanRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public TrainingPlanServiceImpl(TrainingPlanRepository trainingPlanRepository, UserRepository userRepository,
            ExerciseRepository exerciseRepository) {
        this.trainingPlanRepository = trainingPlanRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public TrainingPlan createPlan(TrainingPlanDto dto, User trainer) {
        User member = userRepository.findById(dto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        boolean isCreatorAdmin = trainer.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        boolean isTargetAdmin = member.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isCreatorAdmin && isTargetAdmin) {
            throw new IllegalArgumentException("Trainers cannot create training plans for Admins.");
        }

        TrainingPlan plan = new TrainingPlan();
        plan.setName(dto.getName());
        plan.setDescription(dto.getDescription());
        plan.setMember(member);
        plan.setTrainer(trainer);
        plan.setStatus(TrainingPlanStatus.ACTIVE);
        plan.setVersion(1);

        mapExercises(dto, plan);

        return trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public TrainingPlan updatePlan(Long originalPlanId, TrainingPlanDto dto, User trainer) {
        TrainingPlan oldPlan = getPlanById(originalPlanId);

        // Archive old plan
        oldPlan.setStatus(TrainingPlanStatus.ARCHIVED);
        trainingPlanRepository.save(oldPlan);

        // Create new plan (versioned)
        TrainingPlan newPlan = new TrainingPlan();
        newPlan.setName(dto.getName());
        newPlan.setDescription(dto.getDescription());
        newPlan.setMember(oldPlan.getMember());
        newPlan.setTrainer(trainer);
        newPlan.setStatus(TrainingPlanStatus.ACTIVE);
        newPlan.setVersion(oldPlan.getVersion() + 1);
        newPlan.setCreationDate(LocalDateTime.now());

        mapExercises(dto, newPlan);

        return trainingPlanRepository.save(newPlan);
    }

    private void mapExercises(TrainingPlanDto dto, TrainingPlan plan) {
        if (dto.getExercises() != null) {
            for (int i = 0; i < dto.getExercises().size(); i++) {
                TrainingPlanExerciseDto exDto = dto.getExercises().get(i);
                // Skip empty entries if any
                if (exDto.getExerciseId() == null)
                    continue;

                Exercise exercise = exerciseRepository.findById(exDto.getExerciseId()).orElseThrow(
                        () -> new IllegalArgumentException("Exercise not found: " + exDto.getExerciseId()));

                TrainingPlanExercise tpe = new TrainingPlanExercise();
                tpe.setExercise(exercise);
                tpe.setSets(exDto.getSets());
                tpe.setReps(exDto.getReps());
                tpe.setNotes(exDto.getNotes());
                tpe.setOrderIndex(i);

                plan.addExercise(tpe);
            }
        }
    }

    @Override
    @Transactional
    public void archivePlan(Long planId) {
        TrainingPlan plan = getPlanById(planId);
        plan.setStatus(TrainingPlanStatus.ARCHIVED);
        trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void unarchivePlan(Long planId) {
        TrainingPlan plan = getPlanById(planId);
        plan.setStatus(TrainingPlanStatus.ACTIVE);
        trainingPlanRepository.save(plan);
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        if (!trainingPlanRepository.existsById(planId)) {
            throw new IllegalArgumentException("Training plan not found");
        }
        trainingPlanRepository.deleteById(planId);
    }

    @Override
    public TrainingPlan getPlanById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Training plan not found"));
    }

    @Override
    public List<TrainingPlan> getPlansByMember(Long memberId) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return trainingPlanRepository.findByMember(member);
    }

    @Override
    public List<TrainingPlan> getActivePlansByMember(Long memberId) {
        User member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return trainingPlanRepository.findByMemberAndStatus(member, TrainingPlanStatus.ACTIVE);
    }

    @Override
    public TrainingPlanDto toDto(TrainingPlan plan) {
        TrainingPlanDto dto = new TrainingPlanDto();
        dto.setId(plan.getId());
        dto.setName(plan.getName());
        dto.setDescription(plan.getDescription());
        dto.setMemberId(plan.getMember().getId());

        List<TrainingPlanExerciseDto> exDtos = plan.getExercises().stream().map(tpe -> {
            TrainingPlanExerciseDto exDto = new TrainingPlanExerciseDto();
            exDto.setExerciseId(tpe.getExercise().getId());
            exDto.setExerciseName(tpe.getExercise().getName());
            exDto.setSets(tpe.getSets());
            exDto.setReps(tpe.getReps());
            exDto.setNotes(tpe.getNotes());
            exDto.setOrderIndex(tpe.getOrderIndex());
            return exDto;
        }).collect(Collectors.toList());

        dto.setExercises(exDtos);
        return dto;
    }
}
