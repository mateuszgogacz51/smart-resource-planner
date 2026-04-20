package pl.gogacz.planner.dto;

import java.time.LocalDateTime;

public class TimelineEvent {
    private LocalDateTime timestamp;
    private String author;
    private String type; // Np. "AUDIT" (zmiana statusu) lub "COMMENT" (notatka)
    private String message;

    public TimelineEvent(LocalDateTime timestamp, String author, String type, String message) {
        this.timestamp = timestamp;
        this.author = author;
        this.type = type;
        this.message = message;
    }

    // Gettery (niezbędne do konwersji na JSON dla Angulara)
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getAuthor() { return author; }
    public String getType() { return type; }
    public String getMessage() { return message; }
}