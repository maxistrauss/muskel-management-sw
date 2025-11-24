package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.MembershipConfirmation;
import de.oth.muskelmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MembershipConfirmationRepository extends JpaRepository<MembershipConfirmation, Long> {
    
    List<MembershipConfirmation> findByUserAndDeletedFalse(User user);
    
    List<MembershipConfirmation> findByDeletedFalse();
    
    List<MembershipConfirmation> findByUser_IdAndDeletedFalse(Long userId);
}