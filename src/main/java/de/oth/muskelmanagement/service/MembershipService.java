package de.oth.muskelmanagement.service;

import de.oth.muskelmanagement.model.entity.Membership;

import java.util.List;

public interface MembershipService {
    List<Membership> findAll();

    Membership findByName(String name);
}
