package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Membership findByName(String name);
}
