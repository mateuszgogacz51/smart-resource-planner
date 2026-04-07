package pl.gogacz.planner.core.model;

public enum ResourceStatus {
    AVAILABLE,   // Dostępny do rezerwacji
    IN_REPAIR,   // W naprawie (zablokowany)
    RETIRED      // Wycofany z użytku
}