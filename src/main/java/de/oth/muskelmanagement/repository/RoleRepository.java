package de.oth.muskelmanagement.repository;

import de.oth.muskelmanagement.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
