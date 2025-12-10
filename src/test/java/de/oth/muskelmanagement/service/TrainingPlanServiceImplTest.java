package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.TrainingPlanDto;
import de.oth.muskelmanagement.dto.TrainingPlanExerciseDto;
import de.oth.muskelmanagement.model.entity.Exercise;
import de.oth.muskelmanagement.model.entity.Role;
import de.oth.muskelmanagement.model.entity.TrainingPlan;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.TrainingPlanStatus;
import de.oth.muskelmanagement.repository.ExerciseRepository;
import de.oth.muskelmanagement.repository.TrainingPlanRepository;
import de.oth.muskelmanagement.repository.UserRepository;
import de.oth.muskelmanagement.service.impl.TrainingPlanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceImplTest {

    @Mock
    private TrainingPlanRepository trainingPlanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private TrainingPlanServiceImpl trainingPlanService;

    private User member;
    private User trainer;
    private TrainingPlanDto trainingPlanDto;
    private Exercise exercise;

    @BeforeEach
    void setUp() {
        member = new User();
        member.setId(1L);
        member.setEmail("member@example.com");
        member.setRoles(Set.of(new Role("ROLE_MEMBER")));

        trainer = new User();
        trainer.setId(2L);
        trainer.setEmail("trainer@example.com");
        trainer.setRoles(Set.of(new Role("ROLE_TRAINER")));

        exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Push Up");

        trainingPlanDto = new TrainingPlanDto();
        trainingPlanDto.setName("Test Plan");
        trainingPlanDto.setDescription("Test Description");
        trainingPlanDto.setMemberId(1L);

        List<TrainingPlanExerciseDto> exercises = new ArrayList<>();
        TrainingPlanExerciseDto exDto = new TrainingPlanExerciseDto();
        exDto.setExerciseId(1L);
        exDto.setSets(3);
        exDto.setReps("10");
        exercises.add(exDto);
        trainingPlanDto.setExercises(exercises);
    }

    @Test
    void createPlan_ShouldCreatePlan_WhenValid() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(member));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingPlan createdPlan = trainingPlanService.createPlan(trainingPlanDto, trainer);

        assertNotNull(createdPlan);
        assertEquals("Test Plan", createdPlan.getName());
        assertEquals(member, createdPlan.getMember());
        assertEquals(trainer, createdPlan.getTrainer());
        assertEquals(1, createdPlan.getExercises().size());
        assertEquals(TrainingPlanStatus.ACTIVE, createdPlan.getStatus());
        verify(trainingPlanRepository).save(any(TrainingPlan.class));
    }

    @Test
    void createPlan_ShouldThrowException_WhenMemberNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> trainingPlanService.createPlan(trainingPlanDto, trainer));
    }

    @Test
    void archivePlan_ShouldSetStatusToArchived() {
        TrainingPlan plan = new TrainingPlan();
        plan.setId(1L);
        plan.setStatus(TrainingPlanStatus.ACTIVE);

        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainingPlanService.archivePlan(1L);

        assertEquals(TrainingPlanStatus.ARCHIVED, plan.getStatus());
        verify(trainingPlanRepository).save(plan);
    }

    @Test
    void updatePlan_ShouldArchiveOldAndCreateNewVersion() {
        TrainingPlan oldPlan = new TrainingPlan();
        oldPlan.setId(1L);
        oldPlan.setVersion(1);
        oldPlan.setStatus(TrainingPlanStatus.ACTIVE);
        oldPlan.setMember(member);
        oldPlan.setTrainer(trainer);

        when(trainingPlanRepository.findById(1L)).thenReturn(Optional.of(oldPlan));
        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(exercise));
        // Capture the save calls. First for archiving old, second for creating new.
        when(trainingPlanRepository.save(any(TrainingPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TrainingPlan newPlan = trainingPlanService.updatePlan(1L, trainingPlanDto, trainer);

        // Verify old plan archived
        assertEquals(TrainingPlanStatus.ARCHIVED, oldPlan.getStatus());

        // Verify new plan created
        assertNotNull(newPlan);
        assertEquals(2, newPlan.getVersion());
        assertEquals(TrainingPlanStatus.ACTIVE, newPlan.getStatus());
        assertEquals("Test Plan", newPlan.getName());

        verify(trainingPlanRepository, times(2)).save(any(TrainingPlan.class));
    }
}
