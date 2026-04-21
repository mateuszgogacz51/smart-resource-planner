package pl.gogacz.planner.core.dto;

import java.util.Map;

public class DashboardStats {
    private long totalReservations;
    private Map<String, Long> statusDistribution;
    private Map<String, Long> employeeRanking;

    public DashboardStats(long totalReservations, Map<String, Long> statusDistribution, Map<String, Long> employeeRanking) {
        this.totalReservations = totalReservations;
        this.statusDistribution = statusDistribution;
        this.employeeRanking = employeeRanking;
    }

    public long getTotalReservations() { return totalReservations; }
    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public Map<String, Long> getEmployeeRanking() { return employeeRanking; }
}