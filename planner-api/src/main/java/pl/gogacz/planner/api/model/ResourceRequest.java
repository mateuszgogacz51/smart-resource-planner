package pl.gogacz.planner.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Wniosek o dodanie lub aktualizację zasobu")
public class ResourceRequest {

    @NotBlank(message = "Nazwa zasobu nie może być pusta")
    @Schema(description = "Nazwa zasobu", example = "Laptop Dell XPS 15")
    private String name;

    @NotBlank(message = "Kategoria zasobu nie może być pusta")
    @Schema(description = "Kategoria zasobu", example = "Sprzęt IT")
    private String category;

    @NotNull(message = "Wartość zasobu jest wymagana")
    @DecimalMin(value = "0.0", inclusive = false, message = "Wartość zasobu musi być większa od zera")
    @Schema(description = "Wartość zasobu w PLN", example = "5999.99")
    private BigDecimal value;

    @Schema(description = "Pobór mocy w kW (opcjonalne)", example = "0.65")
    private BigDecimal powerConsumptionKw;

    @Schema(description = "Czy zasób wymaga wysokiego napięcia", example = "false")
    private boolean requiresHighVoltage;

    @Schema(description = "Czy zasób posiada certyfikat ATEX", example = "false")
    private boolean atexCertified;

    @Schema(description = "Czy zasób posiada certyfikat SEP", example = "false")
    private boolean hasSepCertification;

    // --- Gettery i Settery ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }

    public BigDecimal getPowerConsumptionKw() { return powerConsumptionKw; }
    public void setPowerConsumptionKw(BigDecimal powerConsumptionKw) { this.powerConsumptionKw = powerConsumptionKw; }

    public boolean isRequiresHighVoltage() { return requiresHighVoltage; }
    public void setRequiresHighVoltage(boolean requiresHighVoltage) { this.requiresHighVoltage = requiresHighVoltage; }

    public boolean isAtexCertified() { return atexCertified; }
    public void setAtexCertified(boolean atexCertified) { this.atexCertified = atexCertified; }

    public boolean isHasSepCertification() { return hasSepCertification; }
    public void setHasSepCertification(boolean hasSepCertification) { this.hasSepCertification = hasSepCertification; }
}
