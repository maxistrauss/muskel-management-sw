package de.oth.muskelmanagement.service.impl;

import de.oth.muskelmanagement.model.entity.Membership;
import de.oth.muskelmanagement.repository.MembershipRepository;
import de.oth.muskelmanagement.service.MembershipService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipServiceImpl implements MembershipService {

    private final MembershipRepository membershipRepository;

    public MembershipServiceImpl(MembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }

    @Override
    public List<Membership> findAll() {
        return membershipRepository.findAll();
    }

    @Override
    public Membership findByName(String name) {
        return membershipRepository.findByName(name);
    }
}
