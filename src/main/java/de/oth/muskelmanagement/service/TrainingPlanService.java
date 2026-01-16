package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.dto.TrainingPlanDto;
import de.oth.muskelmanagement.model.entity.TrainingPlan;
import de.oth.muskelmanagement.model.entity.User;

import java.util.List;

public interface TrainingPlanService {
    TrainingPlan createPlan(TrainingPlanDto trainingPlanDto, User trainer);

    TrainingPlan updatePlan(Long originalPlanId, TrainingPlanDto trainingPlanDto, User trainer);

    void archivePlan(Long planId);

    void unarchivePlan(Long planId);

    TrainingPlan getPlanById(Long id);

    List<TrainingPlan> getPlansByMember(Long memberId);

    List<TrainingPlan> getActivePlansByMember(Long memberId);

    TrainingPlanDto toDto(TrainingPlan plan);
}
