package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    User findByEmail(String email);

    List<User> findByEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String email, String firstName, String lastName);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = 'ROLE_MEMBER' AND u.id NOT IN (SELECT u2.id FROM User u2 JOIN u2.roles r2 WHERE r2.name IN ('ROLE_ADMIN', 'ROLE_TRAINER'))")
    org.springframework.data.domain.Page<User> findPureMembers(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT u FROM User u WHERE u.id NOT IN (SELECT u2.id FROM User u2 JOIN u2.roles r WHERE r.name = 'ROLE_ADMIN')")
    org.springframework.data.domain.Page<User> findNonAdmins(org.springframework.data.domain.Pageable pageable);
}
