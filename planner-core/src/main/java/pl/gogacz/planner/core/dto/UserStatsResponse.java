package pl.gogacz.planner.core.dto;

public class UserStatsResponse {
    private String username;
    private String email;
    private String role;
    private int totalApplications;
    private int acceptedApplications;
    private int pendingApplications;
    private int rejectedApplications;

    // Puste konstruktory dla Springa
    public UserStatsResponse() {}

    // Gettery i Settery
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    public int getAcceptedApplications() { return acceptedApplications; }
    public void setAcceptedApplications(int acceptedApplications) { this.acceptedApplications = acceptedApplications; }

    public int getPendingApplications() { return pendingApplications; }
    public void setPendingApplications(int pendingApplications) { this.pendingApplications = pendingApplications; }

    public int getRejectedApplications() { return rejectedApplications; }
    public void setRejectedApplications(int rejectedApplications) { this.rejectedApplications = rejectedApplications; }
}