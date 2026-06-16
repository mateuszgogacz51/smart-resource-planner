package pl.gogacz.planner.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "Wniosek o rezerwację zasobu")
public class ReservationRequest {

    @NotNull(message = "ID zasobu jest wymagane")
    @Schema(description = "ID rezerwowanego zasobu", example = "1")
    private Long resourceId;

    @NotBlank(message = "Cel rezerwacji nie może być pusty")
    @Schema(description = "Cel rezerwacji", example = "Wyjazd służbowy do Krakowa")
    private String purpose;

    @NotNull(message = "Data rozpoczęcia jest wymagana")
    @Future(message = "Data rozpoczęcia musi być w przyszłości")
    @Schema(description = "Data i czas rozpoczęcia rezerwacji", example = "2026-07-01T08:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "Data zakończenia jest wymagana")
    @Future(message = "Data zakończenia musi być w przyszłości")
    @Schema(description = "Data i czas zakończenia rezerwacji", example = "2026-07-05T17:00:00")
    private LocalDateTime endTime;

    @NotBlank(message = "Login wnioskującego nie może być pusty")
    @Schema(description = "Login użytkownika składającego wniosek", example = "jan.kowalski")
    private String requesterUsername;

    // --- Gettery i Settery ---
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getRequesterUsername() { return requesterUsername; }
    public void setRequesterUsername(String requesterUsername) { this.requesterUsername = requesterUsername; }
}
