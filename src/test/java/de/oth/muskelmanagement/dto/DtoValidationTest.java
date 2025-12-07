package de.oth.muskelmanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registrationDto_shouldPassValidation_whenValid() {
        RegistrationDto dto = new RegistrationDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password");

        Set<ConstraintViolation<RegistrationDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void userDto_shouldPassValidation_whenValid() {
        UserDto dto = new UserDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setRoles(Set.of("ROLE_MEMBER"));

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}
