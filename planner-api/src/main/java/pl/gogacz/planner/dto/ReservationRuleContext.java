package pl.gogacz.planner.dto;

import java.time.LocalDateTime;

public class ReservationRuleContext {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean valid = true;
    private String rejectionReason;

    // --- Standardowe Gettery i Settery (zamiast Lomboka) ---

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
}