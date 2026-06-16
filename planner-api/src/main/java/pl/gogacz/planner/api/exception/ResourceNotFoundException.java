package pl.gogacz.planner.api.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Long id) {
        super("Nie znaleziono zasobu o ID: " + id);
    }
}
