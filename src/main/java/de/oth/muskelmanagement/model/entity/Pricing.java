package de.oth.muskelmanagement.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "pricings")
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer durationMonths;

    private String description;

    private boolean active = true;

    @Column(name = "stripe_buy_button_id")
    private String stripeBuyButtonId;

    @Column(name = "stripe_price_id")
    private String stripePriceId;

    public Pricing() {
    }

    public Pricing(String name, BigDecimal price, Integer durationMonths, String description, boolean active) {
        this.name = name;
        this.price = price;
        this.durationMonths = durationMonths;
        this.description = description;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStripeBuyButtonId() {
        return stripeBuyButtonId;
    }

    public void setStripeBuyButtonId(String stripeBuyButtonId) {
        this.stripeBuyButtonId = stripeBuyButtonId;
    }

    public String getStripePriceId() {
        return stripePriceId;
    }

    public void setStripePriceId(String stripePriceId) {
        this.stripePriceId = stripePriceId;
    }
}

