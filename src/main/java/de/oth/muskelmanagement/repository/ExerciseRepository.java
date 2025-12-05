package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    Optional<Exercise> findByExternalId(String externalId);
    
    List<Exercise> findByNameContainingIgnoreCase(String name);
    
    List<Exercise> findByBodyPart(String bodyPart);
    
    List<Exercise> findByEquipment(String equipment);
    
    List<Exercise> findByTarget(String target);
    
    Page<Exercise> findByBodyPart(String bodyPart, Pageable pageable);
    
    Page<Exercise> findByEquipment(String equipment, Pageable pageable);
    
    Page<Exercise> findByTarget(String target, Pageable pageable);
    
    Page<Exercise> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT DISTINCT e.bodyPart FROM Exercise e WHERE e.bodyPart IS NOT NULL ORDER BY e.bodyPart")
    List<String> findDistinctBodyParts();
    
    @Query("SELECT DISTINCT e.equipment FROM Exercise e WHERE e.equipment IS NOT NULL ORDER BY e.equipment")
    List<String> findDistinctEquipment();
    
    @Query("SELECT DISTINCT e.target FROM Exercise e WHERE e.target IS NOT NULL ORDER BY e.target")
    List<String> findDistinctTargets();
    
    @Query("SELECT e FROM Exercise e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.bodyPart) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.equipment) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(e.target) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Exercise> searchExercises(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    long countByBodyPart(String bodyPart);
    
    long countByEquipment(String equipment);
    
    long countByTarget(String target);
}