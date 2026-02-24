package pl.gogacz.planner.api.model;

import java.io.Serializable; // Import interfejsu do przesyłania danych
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ResourceDTO implements Serializable {
    // Statyczny identyfikator wersji (zalecany dla Serializable)
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String category;
    private BigDecimal powerConsumptionKw;
    private boolean requiresHighVoltage;
    private boolean isAtexCertified;
    private LocalDateTime lastTechnicalInspection;
    private BigDecimal value;
    private boolean available;
    private String requesterRole;
    private boolean hasSepCertification;

    public ResourceDTO() {}

    // --- GETTERY I SETTERY ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPowerConsumptionKw() { return powerConsumptionKw; }
    public void setPowerConsumptionKw(BigDecimal powerConsumptionKw) { this.powerConsumptionKw = powerConsumptionKw; }

    public boolean isRequiresHighVoltage() { return requiresHighVoltage; }
    public void setRequiresHighVoltage(boolean requiresHighVoltage) { this.requiresHighVoltage = requiresHighVoltage; }

    public boolean isAtexCertified() { return isAtexCertified; }
    public void setAtexCertified(boolean atexCertified) { this.isAtexCertified = isAtexCertified; }

    public LocalDateTime getLastTechnicalInspection() { return lastTechnicalInspection; }
    public void setLastTechnicalInspection(LocalDateTime lastTechnicalInspection) { this.lastTechnicalInspection = lastTechnicalInspection; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getRequesterRole() { return requesterRole; }
    public void setRequesterRole(String requesterRole) { this.requesterRole = requesterRole; }

    public boolean isHasSepCertification() { return hasSepCertification; }
    public void setHasSepCertification(boolean hasSepCertification) { this.hasSepCertification = hasSepCertification; }
}