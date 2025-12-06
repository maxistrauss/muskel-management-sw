package de.oth.muskelmanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registrationDto_shouldFailValidation_whenMembershipIdIsNull() {
        RegistrationDto dto = new RegistrationDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password");
        dto.setMembershipId(null); // Invalid

        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("membershipId")));
    }

    @Test
    void registrationDto_shouldPassValidation_whenMembershipIdIsProvided() {
        RegistrationDto dto = new RegistrationDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password");
        dto.setMembershipId(1L); // Valid

        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void userDto_shouldFailValidation_whenMembershipIdIsNull() {
        UserDto dto = new UserDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setRoles(Set.of("ROLE_MEMBER"));
        dto.setMembershipId(null); // Invalid

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("membershipId")));
    }
}
