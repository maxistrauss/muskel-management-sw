package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.TrainingPlan;
import de.oth.muskelmanagement.model.entity.User;
import de.oth.muskelmanagement.model.enums.TrainingPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    List<TrainingPlan> findByMember(User member);

    List<TrainingPlan> findByMemberAndStatus(User member, TrainingPlanStatus status);

    List<TrainingPlan> findByTrainer(User trainer);
}
