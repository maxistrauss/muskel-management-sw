package de.oth.muskelmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.Set;

public class UserDto {

    private Long id;

    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotBlank(message = "Email cannot be blank")

    @Email(message = "Please provide a valid email address")

    private String email;

    private String activeSubscriptionPricingName; // Derived from active subscription

    private String activeSubscriptionStatus; // Derived from active subscription

    private java.time.LocalDate activeSubscriptionStartDate; // Derived from active subscription

    private String password;

    private boolean enabled;

    private String deactivationReason;

    private boolean twoFactorEnabled;

    @NotEmpty(message = "User must have at least one role")

    private Set<String> roles;

    public UserDto() {

    }

    public UserDto(Long id, String firstName, String lastName, String email, String activeSubscriptionPricingName,

            String activeSubscriptionStatus, LocalDate activeSubscriptionStartDate, String password, boolean enabled,

            String deactivationReason, boolean twoFactorEnabled, Set<String> roles) {

        this.id = id;

        this.firstName = firstName;

        this.lastName = lastName;

        this.email = email;

        this.activeSubscriptionPricingName = activeSubscriptionPricingName;

        this.activeSubscriptionStatus = activeSubscriptionStatus;

        this.activeSubscriptionStartDate = activeSubscriptionStartDate;

        this.password = password;

        this.enabled = enabled;

        this.deactivationReason = deactivationReason;

        this.twoFactorEnabled = twoFactorEnabled;

        this.roles = roles;

    }

    public Long getId() {

        return id;

    }

    public void setId(Long id) {

        this.id = id;

    }

    public String getFirstName() {

        return firstName;

    }

    public void setFirstName(String firstName) {

        this.firstName = firstName;

    }

    public String getLastName() {

        return lastName;

    }

    public void setLastName(String lastName) {

        this.lastName = lastName;

    }

    public String getEmail() {

        return email;

    }

    public void setEmail(String email) {

        this.email = email;

    }

    public String getActiveSubscriptionPricingName() {

        return activeSubscriptionPricingName;

    }

    public void setActiveSubscriptionPricingName(String activeSubscriptionPricingName) {

        this.activeSubscriptionPricingName = activeSubscriptionPricingName;

    }

    public String getActiveSubscriptionStatus() {

        return activeSubscriptionStatus;

    }

    public void setActiveSubscriptionStatus(String activeSubscriptionStatus) {

        this.activeSubscriptionStatus = activeSubscriptionStatus;

    }

    public LocalDate getActiveSubscriptionStartDate() {

        return activeSubscriptionStartDate;

    }

    public void setActiveSubscriptionStartDate(LocalDate activeSubscriptionStartDate) {

        this.activeSubscriptionStartDate = activeSubscriptionStartDate;

    }

    public String getPassword() {

        return password;

    }

    public void setPassword(String password) {

        this.password = password;

    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public String getDeactivationReason() {
        return deactivationReason;
    }

    public void setDeactivationReason(String deactivationReason) {
        this.deactivationReason = deactivationReason;
    }
}
