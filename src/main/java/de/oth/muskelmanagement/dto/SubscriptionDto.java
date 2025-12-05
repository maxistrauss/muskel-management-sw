package de.oth.muskelmanagement.dto;

import de.oth.muskelmanagement.model.enums.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SubscriptionDto {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long tarifId;
    private String tarifName;
    private BigDecimal tarifPrice;
    private Integer tarifDuration;
    private String tarifDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SubscriptionDto() {
    }

    public SubscriptionDto(Long id, Long userId, String userEmail, String userName, Long tarifId, String tarifName,
            BigDecimal tarifPrice, Integer tarifDuration, String tarifDescription, LocalDate startDate,
            LocalDate endDate, SubscriptionStatus status, Boolean autoRenew, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.tarifId = tarifId;
        this.tarifName = tarifName;
        this.tarifPrice = tarifPrice;
        this.tarifDuration = tarifDuration;
        this.tarifDescription = tarifDescription;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.autoRenew = autoRenew;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Computed properties

    public Long getDaysRemaining() {
        if (endDate == null || status != SubscriptionStatus.ACTIVE) {
            return 0L;
        }
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(today, endDate);
    }

    public Boolean getIsExpiringSoon() {
        if (status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        Long daysRemaining = getDaysRemaining();
        return daysRemaining != null && daysRemaining > 0 && daysRemaining <= 7;
    }

    public Boolean getIsActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public Boolean getIsExpired() {
        return status == SubscriptionStatus.EXPIRED;
    }

    public Boolean getIsCancelled() {
        return status == SubscriptionStatus.CANCELLED;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getTarifId() {
        return tarifId;
    }

    public void setTarifId(Long tarifId) {
        this.tarifId = tarifId;
    }

    public String getTarifName() {
        return tarifName;
    }

    public void setTarifName(String tarifName) {
        this.tarifName = tarifName;
    }

    public BigDecimal getTarifPrice() {
        return tarifPrice;
    }

    public void setTarifPrice(BigDecimal tarifPrice) {
        this.tarifPrice = tarifPrice;
    }

    public Integer getTarifDuration() {
        return tarifDuration;
    }

    public void setTarifDuration(Integer tarifDuration) {
        this.tarifDuration = tarifDuration;
    }

    public String getTarifDescription() {
        return tarifDescription;
    }

    public void setTarifDescription(String tarifDescription) {
        this.tarifDescription = tarifDescription;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public Boolean getAutoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(Boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
