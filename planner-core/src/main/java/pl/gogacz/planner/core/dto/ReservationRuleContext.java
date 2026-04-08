package pl.gogacz.planner.dto;

import lombok.Data;
import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class ReservationRuleContext {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean valid = true;
    private String rejectionReason;

    public long getDurationInHours() {
        if (startTime == null || endTime == null) return 0;
        return Duration.between(startTime, endTime).toHours();
    }
}